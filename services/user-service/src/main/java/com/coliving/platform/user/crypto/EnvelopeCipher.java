package com.coliving.platform.user.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * users.email, name, phone, address 의 Envelope Encryption.
 *
 * <p>KEK 는 {@code app.crypto.key} (Base64 32바이트, 운영은 USER_CRYPTO_KEY) 이며 DB에 두지 않는다.
 * 저장할 때마다 32바이트 DEK 를 만들고, DEK 로 평문을 AES-256-GCM 암호화한 뒤 DEK 는 KEK 로 감싼다.
 * 컬럼에는 감싼 DEK 와 암호문만 남긴다.
 *
 * <p>저장 형식은 {@code env1.} + Base64 이다.
 * version(1) | kekVersion(1) | wrapIv(12) | wrappedDek(48) | dataIv(12) | ciphertext.
 * 접두사가 없는 값은 기존 평문이며 그대로 읽는다.
 */
@Component
public class EnvelopeCipher {

    static final String PREFIX = "env1.";
    static final int VERSION = 1;
    static final int KEK_VERSION = 1;
    static final int IV_LENGTH = 12;
    static final int DEK_LENGTH = 32;
    static final int TAG_BITS = 128;
    static final int TAG_LENGTH = TAG_BITS / 8;
    static final int WRAPPED_DEK_LENGTH = DEK_LENGTH + TAG_LENGTH;
    static final int HEADER_LENGTH = 2 + IV_LENGTH + WRAPPED_DEK_LENGTH + IV_LENGTH;

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final byte[] WRAP_AAD = "user-pii-dek".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DATA_AAD = "user-pii".getBytes(StandardCharsets.UTF_8);

    private final SecretKey kek;
    private final SecureRandom random = new SecureRandom();

    public EnvelopeCipher(@Value("${app.crypto.key}") String base64Key) {
        byte[] keyBytes = decodeKey(base64Key);
        this.kek = new SecretKeySpec(keyBytes, "AES");
        Arrays.fill(keyBytes, (byte) 0);
    }

    public String encrypt(String plain) {
        if (plain == null) {
            return null;
        }
        byte[] dek = new byte[DEK_LENGTH];
        byte[] wrapIv = new byte[IV_LENGTH];
        byte[] dataIv = new byte[IV_LENGTH];
        random.nextBytes(dek);
        random.nextBytes(wrapIv);
        random.nextBytes(dataIv);
        SecretKey dekKey = new SecretKeySpec(dek, "AES");
        try {
            byte[] wrappedDek = transform(Cipher.ENCRYPT_MODE, kek, wrapIv, WRAP_AAD, dek);
            byte[] ciphertext = transform(
                    Cipher.ENCRYPT_MODE,
                    dekKey,
                    dataIv,
                    DATA_AAD,
                    plain.getBytes(StandardCharsets.UTF_8));
            if (wrappedDek.length != WRAPPED_DEK_LENGTH) {
                throw new IllegalStateException("암호화에 실패했습니다.");
            }
            byte[] payload = new byte[HEADER_LENGTH + ciphertext.length];
            payload[0] = VERSION;
            payload[1] = KEK_VERSION;
            int offset = 2;
            System.arraycopy(wrapIv, 0, payload, offset, wrapIv.length);
            offset += wrapIv.length;
            System.arraycopy(wrappedDek, 0, payload, offset, wrappedDek.length);
            offset += wrappedDek.length;
            System.arraycopy(dataIv, 0, payload, offset, dataIv.length);
            offset += dataIv.length;
            System.arraycopy(ciphertext, 0, payload, offset, ciphertext.length);
            return PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("암호화에 실패했습니다.", ex);
        } finally {
            Arrays.fill(dek, (byte) 0);
            destroy(dekKey);
        }
    }

    public String decrypt(String stored) {
        if (stored == null) {
            return null;
        }
        if (!stored.startsWith(PREFIX)) {
            return stored;
        }
        byte[] payload;
        try {
            payload = Base64.getDecoder().decode(stored.substring(PREFIX.length()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("복호화에 실패했습니다.", ex);
        }
        if (payload.length < HEADER_LENGTH + TAG_LENGTH || payload[0] != VERSION || payload[1] != KEK_VERSION) {
            throw new IllegalStateException("복호화에 실패했습니다.");
        }
        byte[] wrapIv = Arrays.copyOfRange(payload, 2, 2 + IV_LENGTH);
        int wrappedOffset = 2 + IV_LENGTH;
        byte[] wrappedDek = Arrays.copyOfRange(payload, wrappedOffset, wrappedOffset + WRAPPED_DEK_LENGTH);
        int dataIvOffset = wrappedOffset + WRAPPED_DEK_LENGTH;
        byte[] dataIv = Arrays.copyOfRange(payload, dataIvOffset, dataIvOffset + IV_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(payload, HEADER_LENGTH, payload.length);
        byte[] dek;
        try {
            dek = transform(Cipher.DECRYPT_MODE, kek, wrapIv, WRAP_AAD, wrappedDek);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("복호화에 실패했습니다.", ex);
        }
        if (dek.length != DEK_LENGTH) {
            Arrays.fill(dek, (byte) 0);
            throw new IllegalStateException("복호화에 실패했습니다.");
        }
        SecretKey dekKey = new SecretKeySpec(dek, "AES");
        try {
            byte[] plain = transform(Cipher.DECRYPT_MODE, dekKey, dataIv, DATA_AAD, ciphertext);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("복호화에 실패했습니다.", ex);
        } finally {
            Arrays.fill(dek, (byte) 0);
            destroy(dekKey);
        }
    }

    private static byte[] decodeKey(String base64Key) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Key);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("app.crypto.key는 Base64로 인코딩한 32바이트여야 합니다.", ex);
        }
        if (keyBytes.length != DEK_LENGTH) {
            Arrays.fill(keyBytes, (byte) 0);
            throw new IllegalStateException("app.crypto.key는 Base64로 인코딩한 32바이트여야 합니다.");
        }
        return keyBytes;
    }

    private static byte[] transform(int mode, SecretKey key, byte[] iv, byte[] aad, byte[] input)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(mode, key, new GCMParameterSpec(TAG_BITS, iv));
        cipher.updateAAD(aad);
        return cipher.doFinal(input);
    }

    private static void destroy(SecretKey key) {
        if (key instanceof Destroyable destroyable) {
            try {
                destroyable.destroy();
            } catch (DestroyFailedException ignored) {
                // 키 구현이 파기를 거부해도 암호 연산은 이미 끝났다.
            }
        }
    }
}

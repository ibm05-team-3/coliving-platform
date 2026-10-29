package com.coliving.platform.common.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 비회원 연락처 AES-256-GCM 암호화 (DB: phone_enc, email_enc)
 * 키: community.crypto.key (Base64, 32바이트) — 운영은 환경 변수 COMMUNITY_CRYPTO_KEY
 */
@Component
public class CryptoService {

	private static final int IV_LENGTH = 12;
	private static final int TAG_BITS = 128;

	private final SecretKeySpec key;
	private final SecureRandom random = new SecureRandom();

	public CryptoService(@Value("${community.crypto.key}") String base64Key) {
		byte[] keyBytes = Base64.getDecoder().decode(base64Key);
		if (keyBytes.length != 32) {
			throw new IllegalStateException("community.crypto.key는 Base64로 인코딩한 32바이트여야 합니다.");
		}
		this.key = new SecretKeySpec(keyBytes, "AES");
	}

	public String encrypt(String plain) {
		try {
			byte[] iv = new byte[IV_LENGTH];
			random.nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
			byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array());
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("암호화에 실패했습니다.", e);
		}
	}

	public String decrypt(String encoded) {
		try {
			byte[] all = Base64.getDecoder().decode(encoded);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, all, 0, IV_LENGTH));
			return new String(cipher.doFinal(all, IV_LENGTH, all.length - IV_LENGTH), StandardCharsets.UTF_8);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("복호화에 실패했습니다.", e);
		}
	}

	/** 요청 제한 키 등에 쓰는 단방향 해시 (원문을 Redis에 남기지 않기 위해) */
	public String sha256(String value) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
	}
}

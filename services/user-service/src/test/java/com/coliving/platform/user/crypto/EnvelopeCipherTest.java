package com.coliving.platform.user.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coliving.platform.user.entity.UserEmailAccountEntity;
import com.coliving.platform.user.entity.UserEntity;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class EnvelopeCipherTest {

    private static final String KEK = Base64.getEncoder().encodeToString(
            "dev-only-user-key-not-for-prod!!".getBytes(StandardCharsets.UTF_8));

    private final EnvelopeCipher cipher = new EnvelopeCipher(KEK);
    private final PersonalDataConverter converter = new PersonalDataConverter(cipher);

    @Test
    void storesEachValueAsItsOwnEnvelopeAndReadsThePlaintext() {
        String email = "admin@naver.com";
        String name = "홍길동";
        String phone = "010-1234-5678";
        String address = "서울특별시 중구 세종대로 110";

        String storedEmail = converter.convertToDatabaseColumn(email);
        String storedName = converter.convertToDatabaseColumn(name);
        String storedPhone = converter.convertToDatabaseColumn(phone);
        String storedAddress = converter.convertToDatabaseColumn(address);

        assertEnvelope(storedEmail, email);
        assertEnvelope(storedName, name);
        assertEnvelope(storedPhone, phone);
        assertEnvelope(storedAddress, address);
        assertEquals(email, converter.convertToEntityAttribute(storedEmail));
        assertEquals(name, converter.convertToEntityAttribute(storedName));
        assertEquals(phone, converter.convertToEntityAttribute(storedPhone));
        assertEquals(address, converter.convertToEntityAttribute(storedAddress));
        assertNotEquals(storedName, converter.convertToDatabaseColumn(name));
    }

    @Test
    void keepsNullAndLegacyPlaintextReadable() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
        assertEquals("", converter.convertToEntityAttribute(converter.convertToDatabaseColumn("")));
        assertEquals("홍길동", cipher.decrypt("홍길동"));
    }

    @Test
    void rejectsATamperedEnvelopeOrAnotherKek() {
        String stored = cipher.encrypt("홍길동");
        byte[] payload = Base64.getDecoder().decode(stored.substring(EnvelopeCipher.PREFIX.length()));
        payload[payload.length - 1] ^= 0x01;
        String tampered = EnvelopeCipher.PREFIX + Base64.getEncoder().encodeToString(payload);
        assertThrows(IllegalStateException.class, () -> cipher.decrypt(tampered));

        byte[] otherKey = new byte[EnvelopeCipher.DEK_LENGTH];
        Arrays.fill(otherKey, (byte) 7);
        EnvelopeCipher other = new EnvelopeCipher(Base64.getEncoder().encodeToString(otherKey));
        assertThrows(IllegalStateException.class, () -> other.decrypt(stored));
    }

    @Test
    void personalColumnsUseTheConverterAndNicknameStaysPlain() throws Exception {
        assertEquals(PersonalDataConverter.class, converterOn("email"));
        assertEquals(PersonalDataConverter.class, converterOn("name"));
        assertEquals(PersonalDataConverter.class, converterOn("phone"));
        assertEquals(PersonalDataConverter.class, converterOn("address"));
        assertEquals("text", UserEntity.class.getDeclaredField("email").getAnnotation(jakarta.persistence.Column.class).columnDefinition());
        assertFalse(UserEntity.class.getDeclaredField("nickname").isAnnotationPresent(Convert.class));
        assertFalse(UserEmailAccountEntity.class.getDeclaredField("email").isAnnotationPresent(Convert.class));
        assertFalse(PersonalDataConverter.class.getAnnotation(Converter.class).autoApply());
    }

    private void assertEnvelope(String stored, String plain) {
        assertTrue(stored.startsWith(EnvelopeCipher.PREFIX));
        assertFalse(stored.contains(plain));
        byte[] payload = Base64.getDecoder().decode(stored.substring(EnvelopeCipher.PREFIX.length()));
        assertEquals(EnvelopeCipher.VERSION, payload[0]);
        assertEquals(EnvelopeCipher.KEK_VERSION, payload[1]);
        assertTrue(payload.length >= EnvelopeCipher.HEADER_LENGTH + EnvelopeCipher.TAG_LENGTH);
        byte[] wrappedDek = Arrays.copyOfRange(
                payload,
                2 + EnvelopeCipher.IV_LENGTH,
                2 + EnvelopeCipher.IV_LENGTH + EnvelopeCipher.WRAPPED_DEK_LENGTH);
        assertEquals(EnvelopeCipher.WRAPPED_DEK_LENGTH, wrappedDek.length);
        assertFalse(Arrays.equals(wrappedDek, new byte[EnvelopeCipher.WRAPPED_DEK_LENGTH]));
    }

    private static Class<?> converterOn(String fieldName) throws Exception {
        Field field = UserEntity.class.getDeclaredField(fieldName);
        return field.getAnnotation(Convert.class).converter();
    }
}

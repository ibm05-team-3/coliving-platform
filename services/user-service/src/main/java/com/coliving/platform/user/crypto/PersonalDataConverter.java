package com.coliving.platform.user.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

/**
 * users.email, name, phone, address 를 Envelope Encryption 으로 저장하고 읽는다.
 * autoApply 가 아니므로 nickname 과 user_email_accounts.email 은 평문으로 남는다.
 */
@Component
@Converter(autoApply = false)
public class PersonalDataConverter implements AttributeConverter<String, String> {

    private final EnvelopeCipher cipher;

    public PersonalDataConverter(EnvelopeCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return cipher.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return cipher.decrypt(dbData);
    }
}

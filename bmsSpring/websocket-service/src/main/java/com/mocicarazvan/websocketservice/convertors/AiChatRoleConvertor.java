package com.mocicarazvan.websocketservice.convertors;


import com.mocicarazvan.websocketservice.enums.AiChatRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AiChatRoleConvertor implements AttributeConverter<AiChatRole, String> {
    @Override
    public String convertToDatabaseColumn(AiChatRole attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getRole();
    }

    @Override
    public AiChatRole convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return AiChatRole.fromRole(dbData);
    }
}

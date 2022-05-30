package main.config;

import main.model.enums.ModerationStatusType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToEnumConverter implements Converter<String, ModerationStatusType> {
    @Override
    public ModerationStatusType convert(String status) {
        try {
            return ModerationStatusType.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

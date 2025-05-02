package name.julatec.ekonomi.storage;

import name.julatec.ekonomi.tribunet.UuidFormatter;

import jakarta.persistence.AttributeConverter;
import java.util.UUID;

public class UuidConverter implements AttributeConverter<UUID, String>, UuidFormatter {

    @Override
    public String convertToDatabaseColumn(UUID attribute) {
        return UuidFormatter.uuidToString(attribute);
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
        return UUID.fromString(dbData);
    }

}

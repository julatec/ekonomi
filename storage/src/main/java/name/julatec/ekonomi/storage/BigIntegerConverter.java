package name.julatec.ekonomi.storage;

import javax.persistence.AttributeConverter;
import java.math.BigInteger;

public class BigIntegerConverter implements AttributeConverter<BigInteger, String> {

    @Override
    public String convertToDatabaseColumn(BigInteger attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public BigInteger convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new BigInteger(dbData);
    }

}

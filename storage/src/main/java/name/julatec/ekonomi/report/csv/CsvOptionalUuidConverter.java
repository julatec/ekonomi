package name.julatec.ekonomi.report.csv;

import com.opencsv.bean.AbstractBeanField;
import name.julatec.ekonomi.tribunet.UuidFormatter;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.UUID;

public class CsvOptionalUuidConverter extends AbstractBeanField<Optional<UUID>, String> {

    @Override
    protected Object convert(String value) {
        if (value == null || value.length() == 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(UuidFormatter.uuidFromString(value));
    }

    @Override
    protected String convertToWrite(Object value) {
        Optional<UUID> optionalUUID = (Optional<UUID>) value;
        return optionalUUID.map(UuidFormatter::uuidToString).orElse(StringUtils.EMPTY);
    }

}

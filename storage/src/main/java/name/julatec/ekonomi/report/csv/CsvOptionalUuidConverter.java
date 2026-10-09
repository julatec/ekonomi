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
        // opencsv entrega `Object`. `UUID.class::cast` verifica cada elemento, a diferencia de un
        // `(Optional<UUID>) value`, que el compilador no puede comprobar y deja pasar cualquier cosa.
        return ((Optional<?>) value).map(UUID.class::cast).map(UuidFormatter::uuidToString).orElse(StringUtils.EMPTY);
    }

}

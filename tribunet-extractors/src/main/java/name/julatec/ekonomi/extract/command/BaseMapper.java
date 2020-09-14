package name.julatec.ekonomi.extract.command;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

public abstract class BaseMapper<S, R> {

    protected String getDefaultString() {
        return null;
    }

    protected Date getDefaultDate() {
        return null;
    }

    protected BigDecimal getDefaultNumber() {
        return null;
    }

    public abstract R of(Optional<R> target, S source);

}

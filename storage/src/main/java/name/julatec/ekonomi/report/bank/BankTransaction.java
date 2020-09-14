package name.julatec.ekonomi.report.bank;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import name.julatec.ekonomi.report.csv.DefaultSeparator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.Long.toHexString;
import static java.lang.String.format;
import static name.julatec.ekonomi.tribunet.UuidFormatter.uuidFromString;

public interface BankTransaction<T extends BankTransaction<T>> {

    static <T extends BankTransaction<T>> Stream<T> stream(Class<T> clazz, InputStream stream, String account) {
        return stream(clazz, stream, account, Optional.empty());
    }

    static <T extends BankTransaction<T>> Stream<T> stream(Class<T> clazz, InputStream stream, String account, Optional<Currency> currency) {
        final Character separator = Optional.ofNullable(clazz.getAnnotation(DefaultSeparator.class))
                .map(DefaultSeparator::value)
                .orElse(',');
        final InputStreamReader reader = new InputStreamReader(stream);
        final CsvToBean<T> csvToBean = new CsvToBeanBuilder(reader)
                .withType(clazz)
                .withIgnoreLeadingWhiteSpace(true)
                .withSeparator(separator)
                .build();
        return csvToBean
                .stream()
                .map(t -> t.setAccount(account))
                .map(t -> currency.map(t::setCurrency).orElse(t));
    }

    default UUID getId() {
        return uuidFromString(format("%s%s%s",
                toHexString(getDate().getTime()),
                Integer.toHexString(String.valueOf(getDocumentNumber()).hashCode()),
                Integer.toHexString(String.valueOf(getAccount()).hashCode())
        ));
    }

    Date getDate();

    Currency getCurrency();

    T setCurrency(Currency currency);

    BigDecimal getAmount();

    String getAccount();

    T setAccount(String account);

    String getDocumentNumber();

    String getDescription();

}

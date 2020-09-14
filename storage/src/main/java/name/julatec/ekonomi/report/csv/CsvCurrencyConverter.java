package name.julatec.ekonomi.report.csv;

import com.opencsv.bean.AbstractBeanField;

import java.util.Currency;
import java.util.Locale;

public class CsvCurrencyConverter extends AbstractBeanField<Currency, String> {

    protected Object convert(String value) {
        if (value == null || value.length() == 0) {
            return Currency.getInstance(Locale.getDefault());
        }
        return Currency.getInstance(value);
    }

}

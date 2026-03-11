package name.julatec.ekonomi.accounting;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RecordKeyTest {

    private static final Currency CRC = Currency.getInstance("CRC");
    private static final Currency USD = Currency.getInstance("USD");

    private static Record.Key key(Date date, BigDecimal amount, Currency currency) {
        return new Record.Key(() -> date, () -> amount, () -> currency);
    }

    private static Date dateAt(long millis) {
        return new Date(millis);
    }

    // ---- Within time tolerance (1 day) ----

    @Test
    void compareTo_sameDateSameAmountSameCurrency_isZero() {
        Date now = new Date(1000000L);
        Record.Key a = key(now, BigDecimal.TEN, CRC);
        Record.Key b = key(now, BigDecimal.TEN, CRC);
        assertEquals(0, a.compareTo(b));
    }

    @Test
    void compareTo_withinOneDayAndWithinTolerance_isZero() {
        long halfDay = TimeUnit.HOURS.toMillis(12);
        Date d1 = dateAt(0L);
        Date d2 = dateAt(halfDay);
        // Within 1 day diff and amount within 2.0
        Record.Key a = key(d1, new BigDecimal("100.00"), CRC);
        Record.Key b = key(d2, new BigDecimal("101.00"), CRC);
        assertEquals(0, a.compareTo(b));
    }

    @Test
    void compareTo_moreThanOneDay_notZero() {
        long twoDays = TimeUnit.DAYS.toMillis(2);
        Date d1 = dateAt(0L);
        Date d2 = dateAt(twoDays);
        Record.Key a = key(d1, new BigDecimal("100.00"), CRC);
        Record.Key b = key(d2, new BigDecimal("100.00"), CRC);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }

    @Test
    void compareTo_differentCurrencies_withinTimeTolerance_notZero() {
        Date now = new Date(1000000L);
        Record.Key a = key(now, BigDecimal.TEN, CRC);
        Record.Key b = key(now, BigDecimal.TEN, USD);
        assertNotEquals(0, a.compareTo(b));
    }

    @Test
    void compareTo_sameTimeSameCurrencyAmountDiffMoreThanTwo_notZero() {
        Date now = new Date(1000000L);
        Record.Key a = key(now, new BigDecimal("100.00"), CRC);
        Record.Key b = key(now, new BigDecimal("103.00"), CRC);
        assertNotEquals(0, a.compareTo(b));
    }

    @Test
    void compareTo_sameTimeSameCurrencyAmountDiffExactlyTwo_isZero() {
        Date now = new Date(1000000L);
        Record.Key a = key(now, new BigDecimal("100.00"), CRC);
        Record.Key b = key(now, new BigDecimal("102.00"), CRC);
        // 102 - 100 = 2.0 which is NOT > 2.0, so returns 0
        assertEquals(0, a.compareTo(b));
    }

    // ---- getters ----

    @Test
    void getDate_returnsValue() {
        Date now = new Date();
        Record.Key k = key(now, BigDecimal.TEN, CRC);
        assertEquals(now, k.getDate());
    }

    @Test
    void getTotal_returnsValue() {
        Record.Key k = key(new Date(), new BigDecimal("99.99"), CRC);
        assertEquals(new BigDecimal("99.99"), k.getTotal());
    }

    @Test
    void getCurrency_returnsValue() {
        Record.Key k = key(new Date(), BigDecimal.TEN, USD);
        assertEquals(USD, k.getCurrency());
    }

    // ---- equals ----

    @Test
    void equals_sameValues_isTrue() {
        Date now = new Date(12345L);
        Record.Key a = key(now, BigDecimal.TEN, CRC);
        Record.Key b = key(now, BigDecimal.TEN, CRC);
        assertEquals(a, b);
    }

    @Test
    void equals_differentDate_isFalse() {
        Record.Key a = key(new Date(1000L), BigDecimal.TEN, CRC);
        Record.Key b = key(new Date(2000L), BigDecimal.TEN, CRC);
        assertNotEquals(a, b);
    }

    @Test
    void equals_sameInstance_isTrue() {
        Record.Key k = key(new Date(), BigDecimal.TEN, CRC);
        assertEquals(k, k);
    }

    @Test
    void equals_null_isFalse() {
        Record.Key k = key(new Date(), BigDecimal.TEN, CRC);
        assertNotEquals(null, k);
    }

    @Test
    void equals_otherClass_isFalse() {
        Record.Key k = key(new Date(), BigDecimal.TEN, CRC);
        assertNotEquals("string", k);
    }

    // ---- hashCode ----

    @Test
    void hashCode_sameValues_isEqual() {
        Date now = new Date(12345L);
        Record.Key a = key(now, BigDecimal.TEN, CRC);
        Record.Key b = key(now, BigDecimal.TEN, CRC);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // ---- toString ----

    @Test
    void toString_doesNotThrow() {
        Record.Key k = key(new Date(), BigDecimal.TEN, CRC);
        assertNotNull(k.toString());
    }
}

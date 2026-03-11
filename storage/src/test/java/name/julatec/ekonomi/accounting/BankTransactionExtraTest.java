package name.julatec.ekonomi.accounting;

import name.julatec.ekonomi.report.bank.BankTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BankTransactionExtraTest {

    private static final Currency CRC = Currency.getInstance("CRC");

    private static name.julatec.ekonomi.accounting.BankTransaction build(
            long dateMs, BigDecimal amount, String docNum, String account) {
        return new name.julatec.ekonomi.accounting.BankTransaction()
                .setDate(new Date(dateMs))
                .setAmount(amount)
                .setCurrency(CRC)
                .setDocumentNumber(docNum)
                .setAccount(account)
                .setDescription("Test desc");
    }

    @Test
    void setGetDate() {
        Date d = new Date(3000L);
        name.julatec.ekonomi.accounting.BankTransaction bt = new name.julatec.ekonomi.accounting.BankTransaction().setDate(d);
        assertSame(d, bt.getDate());
    }

    @Test
    void setGetDocumentNumber() {
        name.julatec.ekonomi.accounting.BankTransaction bt = new name.julatec.ekonomi.accounting.BankTransaction().setDocumentNumber("DOC-T001");
        assertEquals("DOC-T001", bt.getDocumentNumber());
    }

    @Test
    void setGetDescription() {
        name.julatec.ekonomi.accounting.BankTransaction bt = new name.julatec.ekonomi.accounting.BankTransaction().setDescription("payment");
        assertEquals("payment", bt.getDescription());
    }

    @Test
    void setGetAmount() {
        name.julatec.ekonomi.accounting.BankTransaction bt = new name.julatec.ekonomi.accounting.BankTransaction().setAmount(new BigDecimal("75.00"));
        assertEquals(new BigDecimal("75.00"), bt.getAmount());
    }

    @Test
    void setGetAccount() {
        name.julatec.ekonomi.accounting.BankTransaction bt = new name.julatec.ekonomi.accounting.BankTransaction().setAccount("acc-T001");
        assertEquals("acc-T001", bt.getAccount());
    }

    @Test
    void setGetCurrency() {
        name.julatec.ekonomi.accounting.BankTransaction bt = new name.julatec.ekonomi.accounting.BankTransaction().setCurrency(CRC);
        assertEquals(CRC, bt.getCurrency());
    }

    @Test
    void getId_withGuid_returnsGuid() {
        UUID id = UUID.randomUUID();
        name.julatec.ekonomi.accounting.BankTransaction bt = new name.julatec.ekonomi.accounting.BankTransaction().setId(id);
        assertEquals(id, bt.getId());
    }

    @Test
    void getId_withoutGuid_generatesFromDateDocAccount() {
        name.julatec.ekonomi.accounting.BankTransaction bt = build(1_500_000L, new BigDecimal("100"), "DOC-GEN", "ACC-GEN");
        UUID id = bt.getId();
        assertNotNull(id);
        assertEquals(id, bt.getId());
    }

    @Test
    void toString_containsFields() {
        name.julatec.ekonomi.accounting.BankTransaction bt = build(1_000_000L, new BigDecimal("50"), "DOC-STR", "ACC-STR");
        String str = bt.toString();
        assertTrue(str.contains("DOC-STR") || str.contains("50"));
    }

    @Test
    void getKey_notNull() {
        name.julatec.ekonomi.accounting.BankTransaction bt = build(1_000_000L, new BigDecimal("100"), "D", "A");
        assertNotNull(bt.getKey());
    }

    @Test
    void compareTo_sameDateSameAmount_isZero() {
        name.julatec.ekonomi.accounting.BankTransaction a = build(1_000_000L, new BigDecimal("100"), "D1", "A1");
        name.julatec.ekonomi.accounting.BankTransaction b = build(1_000_000L, new BigDecimal("100"), "D2", "A2");
        assertEquals(0, a.compareTo(b));
    }

    @Test
    void compareTo_differentDate_notZero() {
        // Use dates > 1 day (86_400_000ms) apart to exceed Record.Key tolerance
        name.julatec.ekonomi.accounting.BankTransaction a = build(0L, new BigDecimal("100"), "D1", "A1");
        name.julatec.ekonomi.accounting.BankTransaction b = build(90_000_000L, new BigDecimal("100"), "D2", "A2");
        assertNotEquals(0, a.compareTo(b));
    }

    @Test
    void of_copiesAllFields() {
        UUID id = UUID.randomUUID();
        BankTransaction<name.julatec.ekonomi.accounting.BankTransaction> source = new name.julatec.ekonomi.accounting.BankTransaction()
                .setDate(new Date(9_000_000L))
                .setAmount(new BigDecimal("333"))
                .setCurrency(CRC)
                .setDocumentNumber("DOC-OF")
                .setAccount("ACC-OF")
                .setDescription("desc-of")
                .setId(id);
        name.julatec.ekonomi.accounting.BankTransaction copy = name.julatec.ekonomi.accounting.BankTransaction.of(source);
        assertEquals("ACC-OF", copy.getAccount());
        assertEquals(new BigDecimal("333"), copy.getAmount());
        assertEquals(CRC, copy.getCurrency());
        assertEquals("DOC-OF", copy.getDocumentNumber());
        assertEquals("desc-of", copy.getDescription());
        assertEquals(id, copy.getId());
    }

    // ---- RecordComparable default method ----

    @Test
    void recordComparable_compareTo_usesKey() {
        name.julatec.ekonomi.accounting.BankTransaction a = build(1_000_000L, new BigDecimal("100"), "D1", "A1");
        name.julatec.ekonomi.accounting.BankTransaction b = build(1_000_000L, new BigDecimal("100"), "D2", "A2");
        // RecordComparable.compareTo delegates to getKey().compareTo(that.getKey())
        RecordComparable ra = a;
        RecordComparable rb = b;
        assertEquals(0, ra.compareTo(rb));
    }
}

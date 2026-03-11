package name.julatec.ekonomi.accounting;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RecordTest {

    private static final Currency CRC = Currency.getInstance("CRC");

    private static Voucher makeVoucher(String clave, Date date, BigDecimal amount) {
        return new Voucher()
                .setClave(clave)
                .setFecha(date)
                .setTotalComprobante(amount)
                .setCurrency(CRC);
    }

    private static BankTransaction makeBankTx(Date date, BigDecimal amount) {
        return new BankTransaction()
                .setDate(date)
                .setAmount(amount)
                .setCurrency(CRC)
                .setDocumentNumber("DOC001")
                .setDescription("Test transaction")
                .setAccount("ACC001");
    }

    // ---- Voucher constructor ----

    @Test
    void constructor_voucher_hasPaperReceipt() {
        Voucher v = makeVoucher("CLAVE1", new Date(), BigDecimal.TEN);
        Record record = new Record(v);
        assertTrue(record.getPaperReceipt().isPresent());
        assertFalse(record.getElectronicReceipt().isPresent());
        assertFalse(record.getBankReceipt().isPresent());
        assertFalse(record.getBankOperation().isPresent());
    }

    @Test
    void constructor_voucher_null_throws() {
        assertThrows(NullPointerException.class, () -> new Record((Voucher) null));
    }

    // ---- BankTransaction constructor ----

    @Test
    void constructor_bankTransaction_hasBankReceipt() {
        BankTransaction bt = makeBankTx(new Date(), BigDecimal.TEN);
        Record record = new Record(bt);
        assertTrue(record.getBankReceipt().isPresent());
        assertFalse(record.getPaperReceipt().isPresent());
        assertFalse(record.getElectronicReceipt().isPresent());
    }

    @Test
    void constructor_bankTransaction_null_throws() {
        assertThrows(NullPointerException.class, () -> new Record((BankTransaction) null));
    }

    // ---- Optional<Record> + Voucher constructor ----

    @Test
    void constructor_emptyRecord_voucher_usesPaperReceipt() {
        Voucher v = makeVoucher("CLAVE1", new Date(), BigDecimal.TEN);
        Record record = new Record(Optional.empty(), v);
        assertTrue(record.getPaperReceipt().isPresent());
    }

    @Test
    void constructor_existingRecord_voucher_inheritsKey() {
        Date now = new Date();
        Voucher v1 = makeVoucher("CLAVE1", now, BigDecimal.TEN);
        Record existing = new Record(v1);

        Voucher v2 = makeVoucher("CLAVE2", now, BigDecimal.ONE);
        Record merged = new Record(Optional.of(existing), v2);
        // Key should come from existing record
        assertEquals(existing.getKey(), merged.getKey());
    }

    // ---- Optional<Record> + BankTransaction constructor ----

    @Test
    void constructor_emptyRecord_bankTransaction_usesBankKey() {
        BankTransaction bt = makeBankTx(new Date(), BigDecimal.TEN);
        Record record = new Record(Optional.empty(), bt);
        assertTrue(record.getBankReceipt().isPresent());
    }

    // ---- setters/getters ----

    @Test
    void setEmitterId_getEmitterId() {
        Record record = new Record(makeVoucher("C", new Date(), BigDecimal.TEN));
        record.setEmitterId(Optional.of("E001"));
        assertEquals(Optional.of("E001"), record.getEmitterId());
    }

    @Test
    void setReceptorId_getReceptorId() {
        Record record = new Record(makeVoucher("C", new Date(), BigDecimal.TEN));
        record.setReceptorId(Optional.of("R001"));
        assertEquals(Optional.of("R001"), record.getReceptorId());
    }

    @Test
    void setEmitterName_getEmitterName() {
        Record record = new Record(makeVoucher("C", new Date(), BigDecimal.TEN));
        record.setEmitterName(Optional.of("ACME Corp"));
        assertEquals(Optional.of("ACME Corp"), record.getEmitterName());
    }

    @Test
    void setReceptorName_getReceptorName() {
        Record record = new Record(makeVoucher("C", new Date(), BigDecimal.TEN));
        record.setReceptorName(Optional.of("Customer"));
        assertEquals(Optional.of("Customer"), record.getReceptorName());
    }

    @Test
    void setId_getId() {
        Record record = new Record(makeVoucher("C", new Date(), BigDecimal.TEN));
        record.setId(42L);
        assertEquals(42L, record.getId());
    }

    @Test
    void getKey_returnsNonNull() {
        Record record = new Record(makeVoucher("C", new Date(), BigDecimal.TEN));
        assertNotNull(record.getKey());
    }

    // ---- compareTo ----

    @Test
    void compareTo_earlierDate_isLess() {
        Date d1 = new Date(0L);
        Date d2 = new Date(TimeUnit.DAYS.toMillis(10));
        Record r1 = new Record(makeVoucher("C1", d1, BigDecimal.TEN));
        Record r2 = new Record(makeVoucher("C2", d2, BigDecimal.TEN));
        assertTrue(r1.compareTo(r2) < 0);
    }

    // ---- toString ----

    @Test
    void toString_doesNotThrow() {
        Record record = new Record(makeVoucher("C", new Date(), BigDecimal.TEN));
        assertNotNull(record.toString());
    }

    // ---- hashCode ----

    @Test
    void hashCode_sameKey_isSame() {
        Date now = new Date(12345L);
        Record r1 = new Record(makeVoucher("C1", now, BigDecimal.TEN));
        Record r2 = new Record(makeVoucher("C2", now, BigDecimal.TEN));
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}

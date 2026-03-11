package name.julatec.ekonomi.accounting;

import name.julatec.ekonomi.tribunet.storage.Documento;
import name.julatec.ekonomi.tribunet.storage.ElectronicReceipt;
import name.julatec.ekonomi.tribunet.storage.Emisor;
import name.julatec.ekonomi.tribunet.storage.Receptor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RecordExtraTest {

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
                .setDocumentNumber("DOC-BT")
                .setDescription("desc")
                .setAccount("ACC-BT");
    }

    private static BankOperation makeBankOp(Date date, BigDecimal amount) {
        return new BankOperation()
                .setDate(date)
                .setTotal(amount)
                .setCurrency(CRC)
                .setDocumentNumber("DOC-OP")
                .setPayment(new EmbeddedTransaction().setAccount("acc").setAmount(amount));
    }

    private static ElectronicReceipt makeElectronicReceipt(Date date, BigDecimal amount) {
        Record.Key key = new Record.Key(() -> date, () -> amount, () -> CRC);
        return new ElectronicReceipt() {
            @Override public Optional<Emisor> getEmisor() { return Optional.empty(); }
            @Override public Optional<Receptor> getReceptor() { return Optional.empty(); }
            @Override public Record.Key getKey() { return key; }
            @Override public String getRecordKey() { return "RK"; }
            @Override public String getRecordSequential() { return "RS"; }
            @Override public String getClave() { return "CLAVE-ER"; }
            @Override public Documento getDocumento() { return null; }
        };
    }

    // ---- ElectronicReceipt constructor ----

    @Test
    void constructor_electronicReceipt_setsElectronicReceipt() {
        ElectronicReceipt er = makeElectronicReceipt(new Date(), BigDecimal.TEN);
        Record record = new Record(er);
        assertTrue(record.getElectronicReceipt().isPresent());
        assertFalse(record.getPaperReceipt().isPresent());
        assertFalse(record.getBankReceipt().isPresent());
        assertFalse(record.getBankOperation().isPresent());
    }

    @Test
    void constructor_electronicReceipt_null_throws() {
        assertThrows(NullPointerException.class, () -> new Record((ElectronicReceipt) null));
    }

    // ---- BankOperation constructor ----

    @Test
    void constructor_bankOperation_setsBankOperation() {
        BankOperation op = makeBankOp(new Date(), new BigDecimal("500"));
        Record record = new Record(op);
        assertTrue(record.getBankOperation().isPresent());
        assertFalse(record.getBankReceipt().isPresent());
        assertFalse(record.getPaperReceipt().isPresent());
        assertFalse(record.getElectronicReceipt().isPresent());
    }

    @Test
    void constructor_bankOperation_null_throws() {
        assertThrows(NullPointerException.class, () -> new Record((BankOperation) null));
    }

    // ---- Optional<Record> + ElectronicReceipt constructor ----

    @Test
    void constructor_emptyRecord_electronicReceipt_setsReceipt() {
        ElectronicReceipt er = makeElectronicReceipt(new Date(), BigDecimal.TEN);
        Record record = new Record(Optional.empty(), er);
        assertTrue(record.getElectronicReceipt().isPresent());
    }

    @Test
    void constructor_existingRecord_electronicReceipt_inheritsKey() {
        Date now = new Date();
        Voucher v = makeVoucher("C1", now, BigDecimal.TEN);
        Record existing = new Record(v);
        ElectronicReceipt er = makeElectronicReceipt(now, BigDecimal.ONE);
        Record merged = new Record(Optional.of(existing), er);
        assertEquals(existing.getKey(), merged.getKey());
        assertTrue(merged.getElectronicReceipt().isPresent());
        assertTrue(merged.getPaperReceipt().isPresent()); // inherited from existing
    }

    // ---- Optional<Record> + BankOperation constructor ----

    @Test
    void constructor_emptyRecord_bankOperation_setsBankOp() {
        BankOperation op = makeBankOp(new Date(), new BigDecimal("200"));
        Record record = new Record(Optional.empty(), op);
        assertTrue(record.getBankOperation().isPresent());
    }

    @Test
    void constructor_existingRecord_bankOperation_mergesBankOp() {
        Date now = new Date();
        BankTransaction bt = makeBankTx(now, BigDecimal.TEN);
        Record existing = new Record(bt);
        BankOperation op = makeBankOp(now, new BigDecimal("200"));
        Record merged = new Record(Optional.of(existing), op);
        assertTrue(merged.getBankOperation().isPresent());
        assertTrue(merged.getBankReceipt().isPresent()); // inherited from existing
    }

    // ---- Optional<Record> + Record constructor ----

    @Test
    void constructor_emptyRecord_record_copiesFields() {
        BankTransaction bt = makeBankTx(new Date(), BigDecimal.TEN);
        Record base = new Record(bt);
        Record copy = new Record(Optional.empty(), base);
        assertTrue(copy.getBankReceipt().isPresent());
    }

    @Test
    void constructor_existingRecord_record_combinesFields() {
        Date now = new Date();
        Voucher v = makeVoucher("C1", now, BigDecimal.TEN);
        Record existing = new Record(v);
        BankTransaction bt = makeBankTx(now, BigDecimal.TEN);
        Record second = new Record(bt);
        Record merged = new Record(Optional.of(existing), second);
        assertTrue(merged.getPaperReceipt().isPresent()); // from existing
        assertTrue(merged.getBankReceipt().isPresent()); // from second
    }

    // ---- setSourceAccount / getSourceAccount / setTargetAccount / getTargetAccount ----

    @Test
    void setGetSourceAccount() {
        Record record = new Record(makeVoucher("C", new Date(), BigDecimal.TEN));
        record.setSourceAccount(Optional.empty());
        assertFalse(record.getSourceAccount().isPresent());
    }

    @Test
    void setGetTargetAccount() {
        Record record = new Record(makeVoucher("C", new Date(), BigDecimal.TEN));
        record.setTargetAccount(Optional.empty());
        assertFalse(record.getTargetAccount().isPresent());
    }

    // ---- equals ----

    @Test
    void equals_sameInstance_isTrue() {
        Record record = new Record(makeVoucher("C", new Date(), BigDecimal.TEN));
        assertEquals(record, record);
    }

    @Test
    void equals_null_isFalse() {
        Record record = new Record(makeVoucher("C", new Date(), BigDecimal.TEN));
        assertNotEquals(null, record);
    }

    @Test
    void equals_differentClass_isFalse() {
        Record record = new Record(makeVoucher("C", new Date(), BigDecimal.TEN));
        assertNotEquals("string", record);
    }

    @Test
    void equals_sameKeyDifferentReceipt_isFalse() {
        Date d = new Date(12345L);
        Record r1 = new Record(makeVoucher("C1", d, BigDecimal.TEN));
        Record r2 = new Record(makeBankTx(d, BigDecimal.TEN));
        // Different paperReceipt/bankReceipt → not equal
        assertNotEquals(r1, r2);
    }
}

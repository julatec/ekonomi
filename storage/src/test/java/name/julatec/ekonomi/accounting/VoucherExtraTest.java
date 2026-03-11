package name.julatec.ekonomi.accounting;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VoucherExtraTest {

    private static final Currency CRC = Currency.getInstance("CRC");

    // ---- of(BankTransaction) ----

    @Test
    void of_bankTransaction_setsFields() {
        UUID id = UUID.randomUUID();
        name.julatec.ekonomi.accounting.BankTransaction bt = new name.julatec.ekonomi.accounting.BankTransaction()
                .setDate(new Date(5_000_000L))
                .setAmount(new BigDecimal("150.00"))
                .setCurrency(CRC)
                .setDocumentNumber("DOC-BT-001")
                .setDescription("Test payment")
                .setAccount("ACC-TEST")
                .setId(id);

        Voucher v = Voucher.of(bt);
        assertEquals("DOC-BT-001", v.getConsecutivo());
        assertEquals(new BigDecimal("150.00").negate(), v.getTotalComprobante());
        assertEquals(CRC, v.getCurrency());
        assertEquals("Test payment", v.getEmisorNombre());
        assertTrue(v.getBankTransaction().isPresent());
        assertEquals(id, v.getBankTransaction().get());
    }

    // ---- setters/getters for optional UUID fields ----

    @Test
    void setGetManualTransaction() {
        UUID id = UUID.randomUUID();
        Voucher v = new Voucher().setManualTransaction(Optional.of(id));
        assertTrue(v.getManualTransaction().isPresent());
        assertEquals(id, v.getManualTransaction().get());
    }

    @Test
    void setGetElectronicTransaction() {
        UUID id = UUID.randomUUID();
        Voucher v = new Voucher().setElectronicTransaction(Optional.of(id));
        assertTrue(v.getElectronicTransaction().isPresent());
        assertEquals(id, v.getElectronicTransaction().get());
    }

    // ---- getters for tax fields ----

    @Test
    void getTotalExcento() {
        Voucher v = new Voucher().setTotalExcento(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("100.00"), v.getTotalExcento());
    }

    @Test
    void getTotalExonerado() {
        Voucher v = new Voucher().setTotalExonerado(new BigDecimal("50.00"));
        assertEquals(new BigDecimal("50.00"), v.getTotalExonerado());
    }

    @Test
    void getTotalImpuestoDevuelto() {
        Voucher v = new Voucher().setTotalImpuestoDevuelto(new BigDecimal("5.00"));
        assertEquals(new BigDecimal("5.00"), v.getTotalImpuestoDevuelto());
    }

    @Test
    void getTotalImpuestoF01() {
        Voucher v = new Voucher().setTotalImpuestoF01(new BigDecimal("1.00"));
        assertEquals(new BigDecimal("1.00"), v.getTotalImpuestoF01());
    }

    @Test
    void getTotalImpuestoF02() {
        Voucher v = new Voucher().setTotalImpuestoF02(new BigDecimal("2.00"));
        assertEquals(new BigDecimal("2.00"), v.getTotalImpuestoF02());
    }

    @Test
    void getTotalImpuestoF04() {
        Voucher v = new Voucher().setTotalImpuestoF04(new BigDecimal("4.00"));
        assertEquals(new BigDecimal("4.00"), v.getTotalImpuestoF04());
    }

    @Test
    void getTotalImpuestoF08() {
        Voucher v = new Voucher().setTotalImpuestoF08(new BigDecimal("8.00"));
        assertEquals(new BigDecimal("8.00"), v.getTotalImpuestoF08());
    }

    @Test
    void getTotalImpuestoF13() {
        Voucher v = new Voucher().setTotalImpuestoF13(new BigDecimal("13.00"));
        assertEquals(new BigDecimal("13.00"), v.getTotalImpuestoF13());
    }

    @Test
    void getTotalOtrosCargos() {
        Voucher v = new Voucher().setTotalOtrosCargos(new BigDecimal("7.00"));
        assertEquals(new BigDecimal("7.00"), v.getTotalOtrosCargos());
    }

    // ---- with() merging null cases (min/max with nulls) ----

    @Test
    void with_bothFechaNull_resultNull() {
        Voucher a = new Voucher().setFecha(null);
        Voucher b = new Voucher().setFecha(null);
        Voucher result = a.with(b);
        assertNull(result.getFecha());
    }

    @Test
    void with_bothClaveNull_resultNull() {
        Voucher a = new Voucher().setClave(null);
        Voucher b = new Voucher().setClave(null);
        Voucher result = a.with(b);
        assertNull(result.getClave());
    }

    @Test
    void with_rightNullClave_takesLeft() {
        Voucher a = new Voucher().setClave("AAA");
        Voucher b = new Voucher().setClave(null);
        Voucher result = a.with(b);
        assertEquals("AAA", result.getClave());
    }

    @Test
    void with_electronicTransactionMerged() {
        UUID id = UUID.randomUUID();
        Voucher a = new Voucher().setElectronicTransaction(Optional.of(id));
        Voucher b = new Voucher();
        Voucher result = a.with(b);
        assertEquals(Optional.of(id), result.getElectronicTransaction());
    }

    @Test
    void with_manualTransactionMerged() {
        UUID id = UUID.randomUUID();
        Voucher a = new Voucher();
        Voucher b = new Voucher().setManualTransaction(Optional.of(id));
        Voucher result = a.with(b);
        assertEquals(Optional.of(id), result.getManualTransaction());
    }

    // ---- compareTo - same clave ----

    @Test
    void compareTo_sameKeyAndClave_isZero() {
        Date now = new Date(10_000L);
        Voucher a = new Voucher().setFecha(now).setTotalComprobante(BigDecimal.TEN).setCurrency(CRC).setClave("SAME");
        Voucher b = new Voucher().setFecha(now).setTotalComprobante(BigDecimal.TEN).setCurrency(CRC).setClave("SAME");
        assertEquals(0, a.compareTo(b));
    }
}

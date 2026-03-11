package name.julatec.ekonomi.accounting;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VoucherTest {

    private static final Currency CRC = Currency.getInstance("CRC");
    private static final Currency USD = Currency.getInstance("USD");

    // ---- setConsecutivo ----

    @Test
    void setConsecutivo_normalString_unchanged() {
        Voucher v = new Voucher().setConsecutivo("00100001010000000001");
        assertEquals("00100001010000000001", v.getConsecutivo());
    }

    @Test
    void setConsecutivo_withLeadingApostrophe_stripped() {
        Voucher v = new Voucher().setConsecutivo("'00100001010000000001");
        assertEquals("00100001010000000001", v.getConsecutivo());
    }

    @Test
    void setConsecutivo_null_staysNull() {
        Voucher v = new Voucher().setConsecutivo(null);
        assertNull(v.getConsecutivo());
    }

    @Test
    void setConsecutivo_emptyString_unchanged() {
        Voucher v = new Voucher().setConsecutivo("");
        assertEquals("", v.getConsecutivo());
    }

    // ---- min / max helpers (via with()) ----

    @Test
    void with_null_clave_takesNonNull() {
        Voucher a = new Voucher().setClave(null);
        Voucher b = new Voucher().setClave("ABC");
        Voucher result = a.with(b);
        assertEquals("ABC", result.getClave());
    }

    @Test
    void with_bothClave_takesMin() {
        Voucher a = new Voucher().setClave("ABC");
        Voucher b = new Voucher().setClave("XYZ");
        Voucher result = a.with(b);
        assertEquals("ABC", result.getClave()); // min("ABC","XYZ") = "ABC"
    }

    @Test
    void with_consecutivo_takesMax() {
        Voucher a = new Voucher().setConsecutivo("00001");
        Voucher b = new Voucher().setConsecutivo("00002");
        Voucher result = a.with(b);
        assertEquals("00002", result.getConsecutivo()); // max("00001","00002") = "00002"
    }

    @Test
    void with_fecha_takesMin() {
        Date earlier = new Date(1000L);
        Date later = new Date(2000L);
        Voucher a = new Voucher().setFecha(later);
        Voucher b = new Voucher().setFecha(earlier);
        Voucher result = a.with(b);
        assertEquals(earlier, result.getFecha()); // min
    }

    @Test
    void with_emisorNombre_takesMax() {
        Voucher a = new Voucher().setEmisorNombre("AAA");
        Voucher b = new Voucher().setEmisorNombre("ZZZ");
        Voucher result = a.with(b);
        assertEquals("ZZZ", result.getEmisorNombre()); // max
    }

    @Test
    void with_totalComprobante_takesMax() {
        Voucher a = new Voucher().setTotalComprobante(new BigDecimal("100"));
        Voucher b = new Voucher().setTotalComprobante(new BigDecimal("200"));
        Voucher result = a.with(b);
        assertEquals(new BigDecimal("200"), result.getTotalComprobante()); // max
    }

    @Test
    void with_bankTransaction_mergesOptionals() {
        UUID uuid = UUID.randomUUID();
        Voucher a = new Voucher().setBankTransaction(Optional.of(uuid));
        Voucher b = new Voucher();
        Voucher result = a.with(b);
        assertEquals(Optional.of(uuid), result.getBankTransaction());
    }

    @Test
    void with_optionalEmpty_returnsThis() {
        Voucher a = new Voucher().setClave("ABC").setTotalComprobante(BigDecimal.TEN);
        Voucher result = a.with(Optional.empty());
        assertEquals("ABC", result.getClave());
        assertEquals(BigDecimal.TEN, result.getTotalComprobante());
    }

    @Test
    void with_optionalPresent_merges() {
        Voucher a = new Voucher().setClave("AAA");
        Voucher b = new Voucher().setClave("ZZZ");
        Voucher result = a.with(Optional.of(b));
        assertEquals("AAA", result.getClave()); // min
    }

    // ---- getKey ----

    @Test
    void getKey_returnsNonNull() {
        Voucher v = new Voucher()
                .setFecha(new Date())
                .setTotalComprobante(BigDecimal.TEN)
                .setCurrency(CRC);
        assertNotNull(v.getKey());
    }

    // ---- compareTo ----

    @Test
    void compareTo_sameKey_differentClave_notZero() {
        Date now = new Date();
        Voucher a = new Voucher()
                .setFecha(now).setTotalComprobante(BigDecimal.TEN).setCurrency(CRC).setClave("AAA");
        Voucher b = new Voucher()
                .setFecha(now).setTotalComprobante(BigDecimal.TEN).setCurrency(CRC).setClave("ZZZ");
        assertTrue(a.compareTo(b) < 0);
    }

    // ---- currency defaults ----

    @Test
    void defaultCurrency_isSystemLocale() {
        Voucher v = new Voucher();
        assertNotNull(v.getCurrency());
    }

    // ---- setters/getters chain ----

    @Test
    void fluent_setters_returnThis() {
        Voucher v = new Voucher()
                .setClave("CLAVE")
                .setConsecutivo("CONSEC")
                .setFecha(new Date())
                .setEmisorNumero("E001")
                .setEmisorNombre("Emisor")
                .setReceptorNumero("R001")
                .setReceptorNombre("Receptor")
                .setTotalExcento(BigDecimal.ZERO)
                .setTotalExonerado(BigDecimal.ZERO)
                .setTotalF01(BigDecimal.ZERO)
                .setTotalF02(BigDecimal.ZERO)
                .setTotalF04(BigDecimal.ZERO)
                .setTotalF08(BigDecimal.ZERO)
                .setTotalF13(BigDecimal.ZERO)
                .setTotalImpuestoF01(BigDecimal.ZERO)
                .setTotalImpuestoF02(BigDecimal.ZERO)
                .setTotalImpuestoF04(BigDecimal.ZERO)
                .setTotalImpuestoF08(BigDecimal.ZERO)
                .setTotalImpuestoF13(BigDecimal.ZERO)
                .setTotalOtrosCargos(BigDecimal.ZERO)
                .setTotalImpuestoDevuelto(BigDecimal.ZERO)
                .setTotalComprobante(BigDecimal.TEN)
                .setCurrency(CRC);

        assertEquals("CLAVE", v.getClave());
        assertEquals("CONSEC", v.getConsecutivo());
        assertEquals("E001", v.getEmisorNumero());
        assertEquals("Emisor", v.getEmisorNombre());
        assertEquals("R001", v.getReceptorNumero());
        assertEquals("Receptor", v.getReceptorNombre());
        assertEquals(BigDecimal.TEN, v.getTotalComprobante());
        assertEquals(CRC, v.getCurrency());
        assertEquals(BigDecimal.ZERO, v.getTotalF01());
        assertEquals(BigDecimal.ZERO, v.getTotalF02());
        assertEquals(BigDecimal.ZERO, v.getTotalF04());
        assertEquals(BigDecimal.ZERO, v.getTotalF08());
        assertEquals(BigDecimal.ZERO, v.getTotalF13());
    }

    @Test
    void toString_doesNotThrow() {
        Voucher v = new Voucher()
                .setFecha(new Date())
                .setTotalComprobante(BigDecimal.TEN)
                .setClave("CLAVE");
        assertNotNull(v.toString());
    }
}

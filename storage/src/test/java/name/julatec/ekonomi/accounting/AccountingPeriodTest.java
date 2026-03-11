package name.julatec.ekonomi.accounting;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class AccountingPeriodTest {

    @Test
    void setGetCode() {
        AccountingPeriod p = new AccountingPeriod().setCode("IVA-202001");
        assertEquals("IVA-202001", p.getCode());
    }

    @Test
    void setGetName() {
        AccountingPeriod p = new AccountingPeriod().setName("Enero 2020");
        assertEquals("Enero 2020", p.getName());
    }

    @Test
    void setGetLower() {
        Date d = new Date(1000L);
        AccountingPeriod p = new AccountingPeriod().setLower(d);
        assertSame(d, p.getLower());
    }

    @Test
    void setGetUpper() {
        Date d = new Date(2000L);
        AccountingPeriod p = new AccountingPeriod().setUpper(d);
        assertSame(d, p.getUpper());
    }

    @Test
    void setGetRegimen() {
        AccountingPeriod p = new AccountingPeriod().setRegimen(AccountingPeriod.Regimen.ValorAgreagado);
        assertEquals(AccountingPeriod.Regimen.ValorAgreagado, p.getRegimen());
    }

    @Test
    void compareTo_laterUpperDate_isGreater() {
        AccountingPeriod early = new AccountingPeriod().setUpper(new Date(1000L));
        AccountingPeriod late = new AccountingPeriod().setUpper(new Date(2000L));
        assertTrue(late.compareTo(early) > 0);
    }

    @Test
    void compareTo_earlierUpperDate_isLess() {
        AccountingPeriod early = new AccountingPeriod().setUpper(new Date(1000L));
        AccountingPeriod late = new AccountingPeriod().setUpper(new Date(2000L));
        assertTrue(early.compareTo(late) < 0);
    }

    @Test
    void compareTo_sameUpperDate_isZero() {
        AccountingPeriod a = new AccountingPeriod().setUpper(new Date(1500L));
        AccountingPeriod b = new AccountingPeriod().setUpper(new Date(1500L));
        assertEquals(0, a.compareTo(b));
    }

    @Test
    void toString_containsCode() {
        AccountingPeriod p = new AccountingPeriod().setCode("RTT-2021");
        assertTrue(p.toString().contains("RTT-2021"));
    }

    // ---- Regimen enum ----

    @Test
    void regimen_ofCode_iva_returnsValorAgregado() {
        assertEquals(AccountingPeriod.Regimen.ValorAgreagado, AccountingPeriod.Regimen.ofCode("IVA"));
    }

    @Test
    void regimen_ofCode_rtt_returnsTradicional() {
        assertEquals(AccountingPeriod.Regimen.Tradicional, AccountingPeriod.Regimen.ofCode("RTT"));
    }

    @Test
    void regimen_ofCode_rts_returnsSimplificado() {
        assertEquals(AccountingPeriod.Regimen.Simplificado, AccountingPeriod.Regimen.ofCode("RTS"));
    }

    @Test
    void regimen_ofCode_rea_returnsEspecialAgropecuario() {
        assertEquals(AccountingPeriod.Regimen.EspecialAgropecuario, AccountingPeriod.Regimen.ofCode("REA"));
    }

    @Test
    void regimen_ofCode_null_returnsNull() {
        assertNull(AccountingPeriod.Regimen.ofCode(null));
    }

    @Test
    void regimen_ofCode_unknown_returnsNull() {
        assertNull(AccountingPeriod.Regimen.ofCode("UNKNOWN"));
    }

    @Test
    void regimen_code_field() {
        assertEquals("IVA", AccountingPeriod.Regimen.ValorAgreagado.code);
        assertEquals("RTT", AccountingPeriod.Regimen.Tradicional.code);
        assertEquals("RTS", AccountingPeriod.Regimen.Simplificado.code);
        assertEquals("REA", AccountingPeriod.Regimen.EspecialAgropecuario.code);
    }

    // ---- RegimenConverter ----

    @Test
    void regimenConverter_convertToDatabaseColumn() {
        AccountingPeriod.RegimenConverter converter = new AccountingPeriod.RegimenConverter();
        assertEquals("IVA", converter.convertToDatabaseColumn(AccountingPeriod.Regimen.ValorAgreagado));
    }

    @Test
    void regimenConverter_convertToEntityAttribute() {
        AccountingPeriod.RegimenConverter converter = new AccountingPeriod.RegimenConverter();
        assertEquals(AccountingPeriod.Regimen.Tradicional, converter.convertToEntityAttribute("RTT"));
    }

    @Test
    void regimenConverter_convertToEntityAttribute_null_returnsNull() {
        AccountingPeriod.RegimenConverter converter = new AccountingPeriod.RegimenConverter();
        assertNull(converter.convertToEntityAttribute(null));
    }
}

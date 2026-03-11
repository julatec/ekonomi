package name.julatec.ekonomi.accounting;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BankOperationTest {

    private static final Currency CRC = Currency.getInstance("CRC");

    private static BankOperation buildOp(long dateMs, BigDecimal total, String docNum, String paymentAccount) {
        EmbeddedTransaction payment = new EmbeddedTransaction()
                .setAccount(paymentAccount)
                .setAmount(total);
        return new BankOperation()
                .setDate(new Date(dateMs))
                .setTotal(total)
                .setCurrency(CRC)
                .setDocumentNumber(docNum)
                .setPayment(payment);
    }

    @Test
    void setGetDate() {
        Date d = new Date(5000L);
        BankOperation op = new BankOperation().setDate(d);
        assertSame(d, op.getDate());
    }

    @Test
    void setGetDocumentNumber() {
        BankOperation op = new BankOperation().setDocumentNumber("DOC-001");
        assertEquals("DOC-001", op.getDocumentNumber());
    }

    @Test
    void setGetTotal() {
        BankOperation op = new BankOperation().setTotal(new BigDecimal("1000.00"));
        assertEquals(new BigDecimal("1000.00"), op.getTotal());
    }

    @Test
    void setGetPrincipal() {
        BankOperation op = new BankOperation().setPrincipal(new BigDecimal("500.00"));
        assertEquals(new BigDecimal("500.00"), op.getPrincipal());
    }

    @Test
    void setGetCurrency() {
        BankOperation op = new BankOperation().setCurrency(CRC);
        assertEquals(CRC, op.getCurrency());
    }

    @Test
    void setGetPayment() {
        EmbeddedTransaction t = new EmbeddedTransaction().setAccount("pay-acc").setAmount(BigDecimal.TEN);
        BankOperation op = new BankOperation().setPayment(t);
        assertSame(t, op.getPayment());
    }

    @Test
    void setGetInterest() {
        EmbeddedTransaction t = new EmbeddedTransaction().setAccount("int-acc").setAmount(BigDecimal.ONE);
        BankOperation op = new BankOperation().setInterest(t);
        assertSame(t, op.getInterest());
    }

    @Test
    void setGetInsurance() {
        EmbeddedTransaction t = new EmbeddedTransaction().setAccount("ins-acc").setAmount(BigDecimal.ONE);
        BankOperation op = new BankOperation().setInsurance(t);
        assertSame(t, op.getInsurance());
    }

    @Test
    void setGetCharges() {
        EmbeddedTransaction t = new EmbeddedTransaction().setAccount("chg-acc").setAmount(BigDecimal.ONE);
        BankOperation op = new BankOperation().setCharges(t);
        assertSame(t, op.getCharges());
    }

    @Test
    void getId_withGuid_returnsGuid() {
        UUID id = UUID.randomUUID();
        BankOperation op = new BankOperation().setId(id);
        assertEquals(id, op.getId());
    }

    @Test
    void getId_withoutGuid_generatesFromDateDocPayment() {
        BankOperation op = buildOp(1_000_000L, new BigDecimal("200.00"), "DOC-555", "acc-999");
        UUID id = op.getId();
        assertNotNull(id);
        // Calling again returns the same
        assertEquals(id, op.getId());
    }

    @Test
    void getKey_notNull() {
        BankOperation op = buildOp(1_000_000L, new BigDecimal("100"), "DOC", "acc");
        assertNotNull(op.getKey());
    }

    @Test
    void compareTo_sameDateSameAmountSameCurrency_isZero() {
        BankOperation a = buildOp(1_000_000L, new BigDecimal("100"), "D1", "A1");
        BankOperation b = buildOp(1_000_000L, new BigDecimal("100"), "D2", "A2");
        assertEquals(0, a.compareTo(b));
    }

    @Test
    void compareTo_laterDate_isGreater() {
        // Use dates > 1 day apart to exceed the Record.Key tolerance
        BankOperation earlier = buildOp(0L, new BigDecimal("100"), "D1", "A1");
        BankOperation later = buildOp(90_000_000L, new BigDecimal("100"), "D2", "A2");
        assertTrue(later.compareTo(earlier) > 0);
    }
}

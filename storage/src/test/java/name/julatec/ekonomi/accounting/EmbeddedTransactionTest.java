package name.julatec.ekonomi.accounting;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddedTransactionTest {

    @Test
    void setGetAccount() {
        EmbeddedTransaction t = new EmbeddedTransaction().setAccount("account-123");
        assertEquals("account-123", t.getAccount());
    }

    @Test
    void setGetAmount() {
        EmbeddedTransaction t = new EmbeddedTransaction().setAmount(new BigDecimal("99.99"));
        assertEquals(new BigDecimal("99.99"), t.getAmount());
    }

    @Test
    void toString_containsAccountAndAmount() {
        EmbeddedTransaction t = new EmbeddedTransaction()
                .setAccount("acc-001")
                .setAmount(new BigDecimal("50.00"));
        String str = t.toString();
        assertTrue(str.contains("acc-001"));
        assertTrue(str.contains("50.00"));
    }

    @Test
    void constants_accountAndAmount() {
        assertEquals("account", EmbeddedTransaction.ACCOUNT);
        assertEquals("amount", EmbeddedTransaction.AMOUNT);
    }
}

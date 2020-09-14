package name.julatec.ekonomi.accounting;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class EmbeddedTransaction {

    public static final String ACCOUNT = "account";
    public static final String AMOUNT = "amount";

    @Column(name = ACCOUNT)
    private String account;

    @Column(name = AMOUNT, precision = 2)
    private BigDecimal amount;

    public String getAccount() {
        return account;
    }

    public EmbeddedTransaction setAccount(String account) {
        this.account = account;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public EmbeddedTransaction setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("account", account)
                .append("amount", amount)
                .toString();
    }
}

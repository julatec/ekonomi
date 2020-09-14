package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "budget_amounts")
public class BudgetAmount {
    private int id;
    private String budgetGuid;
    private String accountGuid;
    private int periodNum;
    private long amountNum;
    private long amountDenom;

    @Id
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public BudgetAmount setId(int id) {
        this.id = id;
        return this;
    }

    @Basic
    @Column(name = "budget_guid", nullable = false, length = 32)
    public String getBudgetGuid() {
        return budgetGuid;
    }

    public BudgetAmount setBudgetGuid(String budgetGuid) {
        this.budgetGuid = budgetGuid;
        return this;
    }

    @Basic
    @Column(name = "account_guid", nullable = false, length = 32)
    public String getAccountGuid() {
        return accountGuid;
    }

    public BudgetAmount setAccountGuid(String accountGuid) {
        this.accountGuid = accountGuid;
        return this;
    }

    @Basic
    @Column(name = "period_num", nullable = false)
    public int getPeriodNum() {
        return periodNum;
    }

    public BudgetAmount setPeriodNum(int periodNum) {
        this.periodNum = periodNum;
        return this;
    }

    @Basic
    @Column(name = "amount_num", nullable = false)
    public long getAmountNum() {
        return amountNum;
    }

    public BudgetAmount setAmountNum(long amountNum) {
        this.amountNum = amountNum;
        return this;
    }

    @Basic
    @Column(name = "amount_denom", nullable = false)
    public long getAmountDenom() {
        return amountDenom;
    }

    public BudgetAmount setAmountDenom(long amountDenom) {
        this.amountDenom = amountDenom;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BudgetAmount that = (BudgetAmount) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(periodNum, that.periodNum)
                .append(amountNum, that.amountNum)
                .append(amountDenom, that.amountDenom)
                .append(budgetGuid, that.budgetGuid)
                .append(accountGuid, that.accountGuid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(budgetGuid)
                .append(accountGuid)
                .append(periodNum)
                .append(amountNum)
                .append(amountDenom)
                .toHashCode();
    }
}

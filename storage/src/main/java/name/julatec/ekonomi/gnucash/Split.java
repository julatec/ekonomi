package name.julatec.ekonomi.gnucash;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.sql.Timestamp;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(name = "splits")
public class Split {
    private String guid;
    private String txGuid;
    private String accountGuid;
    private String memo;
    private String action;
    private String reconcileState;
    private Timestamp reconcileDate;
    private long valueNum;
    private long valueDenom;
    private long quantityNum;
    private long quantityDenom;
    private String lotGuid;

    @Id
    @Column(name = "guid", nullable = false, length = 32)
    public String getGuid() {
        return guid;
    }

    public Split setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    @Basic
    @Column(name = "tx_guid", nullable = false, length = 32)
    public String getTxGuid() {
        return txGuid;
    }

    public Split setTxGuid(String txGuid) {
        this.txGuid = txGuid;
        return this;
    }

    @Basic
    @Column(name = "account_guid", nullable = false, length = 32)
    public String getAccountGuid() {
        return accountGuid;
    }

    public Split setAccountGuid(String accountGuid) {
        this.accountGuid = accountGuid;
        return this;
    }

    @Basic
    @Column(name = "memo", nullable = false, length = 2048)
    public String getMemo() {
        return memo;
    }

    public Split setMemo(String memo) {
        this.memo = memo;
        return this;
    }

    @Basic
    @Column(name = "action", nullable = false, length = 2048)
    public String getAction() {
        return action;
    }

    public Split setAction(String action) {
        this.action = action;
        return this;
    }

    @Basic
    @Column(name = "reconcile_state", nullable = false, length = 1)
    public String getReconcileState() {
        return reconcileState;
    }

    public Split setReconcileState(String reconcileState) {
        this.reconcileState = reconcileState;
        return this;
    }

    @Basic
    @Column(name = "reconcile_date", nullable = true)
    public Timestamp getReconcileDate() {
        return reconcileDate;
    }

    public Split setReconcileDate(Timestamp reconcileDate) {
        this.reconcileDate = reconcileDate;
        return this;
    }

    @Basic
    @Column(name = "value_num", nullable = false)
    public long getValueNum() {
        return valueNum;
    }

    public Split setValueNum(long valueNum) {
        this.valueNum = valueNum;
        return this;
    }

    @Basic
    @Column(name = "value_denom", nullable = false)
    public long getValueDenom() {
        return valueDenom;
    }

    public Split setValueDenom(long valueDenom) {
        this.valueDenom = valueDenom;
        return this;
    }

    @Basic
    @Column(name = "quantity_num", nullable = false)
    public long getQuantityNum() {
        return quantityNum;
    }

    public Split setQuantityNum(long quantityNum) {
        this.quantityNum = quantityNum;
        return this;
    }

    @Basic
    @Column(name = "quantity_denom", nullable = false)
    public long getQuantityDenom() {
        return quantityDenom;
    }

    public Split setQuantityDenom(long quantityDenom) {
        this.quantityDenom = quantityDenom;
        return this;
    }

    @Basic
    @Column(name = "lot_guid", nullable = true, length = 32)
    public String getLotGuid() {
        return lotGuid;
    }

    public Split setLotGuid(String lotGuid) {
        this.lotGuid = lotGuid;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Split split = (Split) o;

        return new EqualsBuilder()
                .append(valueNum, split.valueNum)
                .append(valueDenom, split.valueDenom)
                .append(quantityNum, split.quantityNum)
                .append(quantityDenom, split.quantityDenom)
                .append(guid, split.guid)
                .append(txGuid, split.txGuid)
                .append(accountGuid, split.accountGuid)
                .append(memo, split.memo)
                .append(action, split.action)
                .append(reconcileState, split.reconcileState)
                .append(reconcileDate, split.reconcileDate)
                .append(lotGuid, split.lotGuid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(guid)
                .append(txGuid)
                .append(accountGuid)
                .append(memo)
                .append(action)
                .append(reconcileState)
                .append(reconcileDate)
                .append(valueNum)
                .append(valueDenom)
                .append(quantityNum)
                .append(quantityDenom)
                .append(lotGuid)
                .toHashCode();
    }
}

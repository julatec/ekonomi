package name.julatec.ekonomi.accounting;

import name.julatec.ekonomi.gnucash.Account;
import name.julatec.ekonomi.tribunet.storage.ElectronicReceipt;
import name.julatec.util.collection.SortMergeJoin;

import org.apache.commons.lang3.builder.*;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.empty;

public class Record implements RecordComparable, Comparable<Record> {

    public static final String EXPORT_NAME = "voucher";
    private static final long TIME_DIFF = TimeUnit.DAYS.toMillis(1);

    private static final SortMergeJoin<Record, BankTransaction, Record> RECORD_BANK_TRANSACTION_RECORD_MERGE_SORT_JOIN =
            SortMergeJoin.of(RecordComparable::compareTo, Record::new, Record::new, RecordComparable::compareTo, BankTransaction::compareTo);

    private static final SortMergeJoin<Record, ElectronicReceipt, Record> RECORD_ELECTRONIC_RECEIPT_RECORD_MERGE_SORT_JOIN =
            SortMergeJoin.of(RecordComparable::compareTo, Record::new, Record::new, RecordComparable::compareTo, ElectronicReceipt::compareTo);

    private static final SortMergeJoin<Record, PaperVoucher, Record> RECORD_TRANSACTION_RECORD_MERGE_SORT_JOIN =
            SortMergeJoin.of(RecordComparable::compareTo, Record::new, Record::new, RecordComparable::compareTo, PaperVoucher::compareTo);

    private static final SortMergeJoin<Record, BankOperation, Record> RECORD_BANK_OPERATION_RECORD_MERGE_SORT_JOIN =
            SortMergeJoin.of(RecordComparable::compareTo, Record::new, Record::new, RecordComparable::compareTo, BankOperation::compareTo);

    private final Key key;
    private final Optional<PaperVoucher> paperReceipt;
    private final Optional<ElectronicReceipt> electronicReceipt;
    private final Optional<BankTransaction> bankReceipt;
    private final Optional<BankOperation> bankOperation;
    private Optional<String> receptorId = empty();
    private Optional<String> emitterId = empty();
    private Optional<String> emitterName = empty();
    private Optional<Account> sourceAccount = empty();
    private Optional<Account> targetAccount = empty();
    private Optional<String> receptorName = empty();
    private transient long id = 0;

    public Record(PaperVoucher receipt) {
        Objects.requireNonNull(receipt);
        this.paperReceipt = Optional.of(receipt);
        this.electronicReceipt = empty();
        this.bankReceipt = empty();
        this.bankOperation = empty();
        this.key = receipt.getKey();
    }

    public Record(ElectronicReceipt receipt) {
        Objects.requireNonNull(receipt);
        this.electronicReceipt = Optional.of(receipt);
        this.bankReceipt = empty();
        this.paperReceipt = empty();
        this.bankOperation = empty();
        this.key = receipt.getKey();
    }

    public Record(BankTransaction receipt) {
        Objects.requireNonNull(receipt);
        this.bankReceipt = Optional.of(receipt);
        this.electronicReceipt = empty();
        this.paperReceipt = empty();
        this.bankOperation = empty();
        this.key = receipt.getKey();
    }

    public Record(BankOperation receipt) {
        Objects.requireNonNull(receipt);
        this.bankReceipt = empty();
        this.electronicReceipt = empty();
        this.paperReceipt = empty();
        this.bankOperation = Optional.of(receipt);
        this.key = receipt.getKey();
    }

    public Record(Optional<Record> record, PaperVoucher receipt) {
        Objects.requireNonNull(record);
        Objects.requireNonNull(receipt);
        this.paperReceipt = Optional.of(receipt);
        this.bankReceipt = record.flatMap(Record::getBankReceipt);
        this.electronicReceipt = record.flatMap(Record::getElectronicReceipt);
        this.bankOperation = record.flatMap(Record::getBankOperation);
        this.key = record.map(Record::getKey).orElseGet(receipt::getKey);
    }

    public Record(Optional<Record> record, ElectronicReceipt receipt) {
        Objects.requireNonNull(record);
        Objects.requireNonNull(receipt);
        this.electronicReceipt = Optional.of(receipt);
        this.bankReceipt = record.flatMap(Record::getBankReceipt);
        this.paperReceipt = record.flatMap(Record::getPaperReceipt);
        this.bankOperation = record.flatMap(Record::getBankOperation);
        this.key = record.map(Record::getKey).orElseGet(receipt::getKey);
    }

    public Record(Optional<Record> record, BankTransaction receipt) {
        Objects.requireNonNull(record);
        Objects.requireNonNull(receipt);
        this.bankReceipt = Optional.of(receipt);
        this.electronicReceipt = record.flatMap(Record::getElectronicReceipt);
        this.paperReceipt = record.flatMap(Record::getPaperReceipt);
        this.bankOperation = record.flatMap(Record::getBankOperation);
        this.key = record.map(Record::getKey).orElseGet(receipt::getKey);
    }

    public Record(Optional<Record> record, BankOperation receipt) {
        Objects.requireNonNull(record);
        Objects.requireNonNull(receipt);
        this.bankReceipt = record.flatMap(Record::getBankReceipt);
        this.electronicReceipt = record.flatMap(Record::getElectronicReceipt);
        this.paperReceipt = record.flatMap(Record::getPaperReceipt);
        this.bankOperation = Optional.of(receipt);
        this.key = record.map(Record::getKey).orElseGet(receipt::getKey);
    }

    public Record(Optional<Record> record, Record receipt) {
        Objects.requireNonNull(record);
        Objects.requireNonNull(receipt);
        this.bankReceipt = first(record, receipt, Record::getBankReceipt);
        this.electronicReceipt = first(record, receipt, Record::getElectronicReceipt);
        this.paperReceipt = first(record, receipt, Record::getPaperReceipt);
        this.bankOperation = first(record, receipt, Record::getBankOperation);
        this.key = record.map(Record::getKey).orElseGet(receipt::getKey);
    }

    private static <T> Optional<T> first(Optional<Record> record, Record receipt, Function<Record, Optional<T>> getter) {
        return record.flatMap(getter).or(() -> getter.apply(receipt));
    }

    public static Stream<Record> mergeWithBank(Stream<Record> recordIterator, Stream<BankTransaction> iterator) {
        return RECORD_BANK_TRANSACTION_RECORD_MERGE_SORT_JOIN.merge(recordIterator, iterator);
    }

    public static Stream<Record> mergeWithElectronic(Stream<Record> recordIterator, Stream<ElectronicReceipt> iterator) {
        return RECORD_ELECTRONIC_RECEIPT_RECORD_MERGE_SORT_JOIN.merge(recordIterator, iterator);
    }

    public static Stream<Record> mergeWithPaper(Stream<Record> recordIterator, Stream<PaperVoucher> iterator) {
        return RECORD_TRANSACTION_RECORD_MERGE_SORT_JOIN.merge(recordIterator, iterator);
    }

    public static Stream<Record> mergeWithOperation(Stream<Record> recordIterator, Stream<BankOperation> iterator) {
        return RECORD_BANK_OPERATION_RECORD_MERGE_SORT_JOIN.merge(recordIterator, iterator);
    }

    public Optional<ElectronicReceipt> getElectronicReceipt() {
        return electronicReceipt;
    }

    public Optional<BankTransaction> getBankReceipt() {
        return bankReceipt;
    }

    public Optional<PaperVoucher> getPaperReceipt() {
        return paperReceipt;
    }

    public Optional<Account> getSourceAccount() {
        return sourceAccount;
    }

    public Optional<BankOperation> getBankOperation() {
        return bankOperation;
    }

    public Record setSourceAccount(Optional<Account> sourceAccount) {
        this.sourceAccount = sourceAccount;
        return this;
    }

    public Optional<Account> getTargetAccount() {
        return targetAccount;
    }

    public Record setTargetAccount(Optional<Account> targetAccount) {
        this.targetAccount = targetAccount;
        return this;
    }

    public Optional<String> getEmitterId() {
        return emitterId;
    }


    public Record setEmitterId(Optional<String> emitterId) {
        this.emitterId = emitterId;
        return this;
    }

    public Optional<String> getEmitterName() {
        return emitterName;
    }

    public Record setEmitterName(Optional<String> emitterName) {
        this.emitterName = emitterName;
        return this;
    }

    public Optional<String> getReceptorId() {
        return receptorId;
    }

    public Record setReceptorId(Optional<String> receptorId) {
        this.receptorId = receptorId;
        return this;
    }

    public Optional<String> getReceptorName() {
        return receptorName;
    }

    public Record setReceptorName(Optional<String> receptorName) {
        this.receptorName = receptorName;
        return this;
    }

    public Key getKey() {
        return key;
    }

    public long getId() {
        return id;
    }

    public Record setId(long id) {
        this.id = id;
        return this;
    }

    @Override
    public int compareTo(Record that) {
        return new CompareToBuilder()
                .append(this.getKey().date.get(), that.getKey().date.get())
                .append(this.getKey().currency.get().getCurrencyCode(),
                        that.getKey().currency.get().getCurrencyCode())
                .append(this.getKey().total.get(), that.getKey().total.get())
                .append(this.id, that.id)
                .toComparison();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Record record = (Record) o;

        return new EqualsBuilder()
                .append(electronicReceipt, record.electronicReceipt)
                .append(bankReceipt, record.bankReceipt)
                .append(paperReceipt, record.paperReceipt)
                .append(key, record.key)
                .append(sourceAccount, record.sourceAccount)
                .append(targetAccount, record.targetAccount)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("key", key)
                .toString();
    }

    public static final class Key implements Comparable<Key> {

        private static final String EXPORT_NAME = "voucherkey";
        public final Supplier<Date> date;
        public final Supplier<BigDecimal> total;
        public final Supplier<Currency> currency;

        public Key(Supplier<Date> date, Supplier<BigDecimal> total, Supplier<Currency> currency) {
            this.date = date;
            this.total = total;
            this.currency = currency;
        }

        public Date getDate() {
            return date.get();
        }

        public BigDecimal getTotal() {
            return total.get();
        }

        public Currency getCurrency() {
            return currency.get();
        }

        @Override
        public int compareTo(Key that) {
            final long timeDistance = this.date.get().getTime() - that.date.get().getTime();
            final int timeComparison = ((Long) this.date.get().getTime()).compareTo(that.date.get().getTime());
            if (Math.abs(timeDistance) > TIME_DIFF) {
                return timeComparison;
            }
            final int currencyComparison = this.currency.get().getCurrencyCode().compareTo(that.currency.get().getCurrencyCode());
            if (currencyComparison != 0) {
                return currencyComparison;
            }
            final double amountDistance = this.total.get().subtract(that.total.get()).doubleValue();
            final int amountComparison = this.total.get().compareTo(that.total.get());
            if (Math.abs(amountDistance) > 2.0) {
                return timeComparison != 0 ? timeComparison : amountComparison;
            }
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            return new EqualsBuilder()
                    .append(date.get(), key.date.get())
                    .append(total.get(), key.total.get())
                    .append(currency.get(), key.currency.get())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(date.get())
                    .append(total.get())
                    .append(currency.get())
                    .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                    .append("date", date.get())
                    .append("total", total.get())
                    .append("currency", currency.get())
                    .toString();
        }

    }

}

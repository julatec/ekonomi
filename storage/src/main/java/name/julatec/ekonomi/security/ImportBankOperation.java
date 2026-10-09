package name.julatec.ekonomi.security;

import name.julatec.ekonomi.report.bank.BankOperation;
import name.julatec.ekonomi.accounting.EmbeddedTransaction;

import jakarta.persistence.Entity;

@Entity(name = "import_bank_operation")
public class ImportBankOperation extends ImportTransaction<ImportBankOperation> {

    protected String insuranceAccountId;
    protected String interestAccountId;
    protected String chargesAccountId;
    protected String clazz;

    public String getGuid() {
        return guid;
    }

    public ImportBankOperation setGuid(String guid) {
        this.guid = guid;
        return this;
    }

    public String getInsuranceAccountId() {
        return insuranceAccountId;
    }

    public ImportBankOperation setInsuranceAccountId(String insuranceAccountId) {
        this.insuranceAccountId = insuranceAccountId;
        return this;
    }

    public String getInterestAccountId() {
        return interestAccountId;
    }

    public ImportBankOperation setInterestAccountId(String interestAccountId) {
        this.interestAccountId = interestAccountId;
        return this;
    }

    public String getChargesAccountId() {
        return chargesAccountId;
    }

    public ImportBankOperation setChargesAccountId(String chargesAccountId) {
        this.chargesAccountId = chargesAccountId;
        return this;
    }

    // `clazz` solo se escribe desde `setClazz(Class<T>)`, con la misma cota, así que lo que
    // guarda es siempre una subclase de BankOperation. El nombre pasa por texto en la base de datos, y
    // eso es lo único que hace imposible comprobarlo en compilación.
    @SuppressWarnings("unchecked")
    public <T extends BankOperation<T>> Class<T> getClazz() throws ClassNotFoundException {
        return (Class<T>) Class.forName(clazz);
    }

    public <T extends BankOperation<T>> ImportBankOperation setClazz(Class<T> clazz) {
        this.clazz = clazz.getName();
        return this;
    }

    public name.julatec.ekonomi.accounting.BankOperation of(name.julatec.ekonomi.report.bank.BankOperation<?> bankOperation) {
        return new name.julatec.ekonomi.accounting.BankOperation()
                .setDate(bankOperation.getDate())
                .setDocumentNumber(bankOperation.getDocumentNumber())
                .setCurrency(bankOperation.getCurrency())
                .setTotal(bankOperation.getTotal())
                .setPrincipal(bankOperation.getPrincipal())
                .setId(bankOperation.getId())
                .setPayment(new EmbeddedTransaction().setAccount(this.getRemoteId()).setAmount(bankOperation.getPayment()))
                .setInterest(new EmbeddedTransaction().setAccount(this.getInterestAccountId()).setAmount(bankOperation.getInterest()))
                .setInsurance(new EmbeddedTransaction().setAccount(this.getInterestAccountId()).setAmount(bankOperation.getInsurance()))
                .setCharges(new EmbeddedTransaction().setAccount(this.getChargesAccountId()).setAmount(bankOperation.getCharges()))
                ;

    }
}

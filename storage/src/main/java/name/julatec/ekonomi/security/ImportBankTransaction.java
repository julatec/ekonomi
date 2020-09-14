package name.julatec.ekonomi.security;

import name.julatec.ekonomi.report.bank.BankTransaction;

import javax.persistence.Entity;

@Entity(name = "import_bank_account")
public class ImportBankTransaction extends ImportTransaction<ImportBankTransaction> {

    protected String clazz;

    public <T extends BankTransaction<T>> Class<T> getClazz() throws ClassNotFoundException {
        return (Class<T>) Class.forName(clazz);
    }

    public <T extends BankTransaction<T>> ImportBankTransaction setClazz(Class<T> clazz) {
        this.clazz = clazz.getName();
        return this;
    }


}

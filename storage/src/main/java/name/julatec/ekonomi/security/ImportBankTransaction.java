package name.julatec.ekonomi.security;

import name.julatec.ekonomi.report.bank.BankTransaction;

import jakarta.persistence.Entity;

@Entity(name = "import_bank_account")
public class ImportBankTransaction extends ImportTransaction<ImportBankTransaction> {

    protected String clazz;

    // `clazz` solo se escribe desde `setClazz(Class<T>)`, con la misma cota, así que lo que
    // guarda es siempre una subclase de BankTransaction. El nombre pasa por texto en la base de datos, y
    // eso es lo único que hace imposible comprobarlo en compilación.
    @SuppressWarnings("unchecked")
    public <T extends BankTransaction<T>> Class<T> getClazz() throws ClassNotFoundException {
        return (Class<T>) Class.forName(clazz);
    }

    public <T extends BankTransaction<T>> ImportBankTransaction setClazz(Class<T> clazz) {
        this.clazz = clazz.getName();
        return this;
    }


}

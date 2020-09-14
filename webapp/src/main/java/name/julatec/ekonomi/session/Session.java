package name.julatec.ekonomi.session;

import name.julatec.ekonomi.report.ReportController;
import name.julatec.ekonomi.security.ImportTransaction;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.SortedSet;

import static name.julatec.ekonomi.tribunet.UuidFormatter.uuidToString;

public class Session {

    String username;
    String tenant;
    SortedSet<Client> clients;
    SortedSet<String> inboxes;
    SortedSet<String> tenants;
    SortedSet<ImportAccount> importAccounts;
    Date lowerDate;
    Date upperDate;
    String client;


    public String getUsername() {
        return username;
    }

    public Session setUsername(String username) {
        this.username = username;
        return this;
    }

    public SortedSet<Client> getClients() {
        return clients;
    }

    public Session setClients(SortedSet<Client> clients) {
        this.clients = clients;
        return this;
    }

    public SortedSet<String> getInboxes() {
        return inboxes;
    }

    public Session setInboxes(SortedSet<String> inboxes) {
        this.inboxes = inboxes;
        return this;
    }

    public Date getLowerDate() {
        return lowerDate;
    }

    public Session setLowerDate(Date lowerDate) {
        this.lowerDate = lowerDate;
        return this;
    }

    public Date getUpperDate() {
        return upperDate;
    }

    public Session setUpperDate(Date upperDate) {
        this.upperDate = upperDate;
        return this;
    }

    public String getClient() {
        return client;
    }

    public Session setClient(String client) {
        this.client = client;
        return this;
    }

    public SortedSet<String> getTenants() {
        return tenants;
    }

    public Session setTenants(SortedSet<String> tenants) {
        this.tenants = tenants;
        return this;
    }

    public String getTenant() {
        return tenant;
    }

    public Session setTenant(String tenant) {
        this.tenant = tenant;
        return this;
    }

    public SortedSet<ImportAccount> getImportAccounts() {
        return importAccounts;
    }

    public Session setImportAccounts(SortedSet<ImportAccount> importAccounts) {
        this.importAccounts = importAccounts;
        return this;
    }

    public static class ImportAccount implements Comparable<ImportAccount> {
        private String key;
        private String name;
        private String endpoint;

        public static ImportAccount of(ImportTransaction<?> importTransaction) {
            return new ImportAccount()
                    .setEndpoint(ReportController.getUploadTransactionReportEndpoint(importTransaction))
                    .setName(importTransaction.getName())
                    .setKey(uuidToString(importTransaction.getAccount()));
        }

        public String getKey() {
            return key;
        }

        public ImportAccount setKey(String key) {
            this.key = key;
            return this;
        }

        public String getName() {
            return name;
        }

        public ImportAccount setName(String name) {
            this.name = name;
            return this;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public ImportAccount setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        @Override
        public int compareTo(ImportAccount that) {
            return new CompareToBuilder().append(this.name, that.name).toComparison();
        }
    }

    public static final class Client implements Comparable<Client> {
        private String id;
        private String name;
        private BigDecimal records;

        public String getId() {
            return id;
        }

        public Client setId(String id) {
            this.id = id;
            return this;
        }

        public String getName() {
            return name;
        }

        public Client setName(String name) {
            this.name = name;
            return this;
        }

        public BigDecimal getRecords() {
            return records;
        }

        public Client setRecords(BigDecimal records) {
            this.records = records;
            return this;
        }

        @Override
        public int compareTo(Client client) {
            return new CompareToBuilder().append(id, client.id).toComparison();
        }
    }
}

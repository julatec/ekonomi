package name.julatec.ekonomi.accounting;

import name.julatec.ekonomi.storage.MultiTenantRepository;

import java.util.NavigableSet;
import java.util.TreeSet;

public interface AccountingPeriodRepository extends MultiTenantRepository<AccountingPeriod, String, AccountingPeriodRepository> {

    default NavigableSet<AccountingPeriod> getPeriods() {
        return new TreeSet<>(findAll());
    }

}

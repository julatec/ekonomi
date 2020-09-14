package name.julatec.ekonomi.storage;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MultiRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected String determineCurrentLookupKey() {
        return MultiTenantRepository.getCurrentTenant();
    }
}

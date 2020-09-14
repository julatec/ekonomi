package name.julatec.ekonomi.gnucash;

import name.julatec.ekonomi.storage.MultiTenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface AccountRepository extends MultiTenantRepository<Account, String, AccountRepository> {

    Optional<Account> findAccountByCode(String code);

    @Query("SELECT a FROM Account a WHERE a.parentGuid is null")
    Account catalog();

}

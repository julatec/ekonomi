package name.julatec.ekonomi.accounting;

import name.julatec.ekonomi.storage.MultiTenantRepository;
import name.julatec.util.algebraic.Interval;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public interface BankTransactionRepository extends MultiTenantRepository<BankTransaction, UUID, BankTransactionRepository> {

    @Query("select f from bank_transaction f where f.date between :lower and :upper")
    List<BankTransaction> search(
            @Param("lower") Date lower,
            @Param("upper") Date upper);

    default Stream<BankTransaction> search(Interval<Date> range) {
        requireNonNull(range);
        return search(range.lower, range.upper).stream();
    }

}

package name.julatec.ekonomi.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoRepositoryBean
public interface MultiTenantRepository<T, ID, Self extends MultiTenantRepository<T, ID, Self>>
        extends JpaRepository<T, ID> {

    ThreadLocal<String> currentTenant = new ThreadLocal<>();

    static void setCurrentDb(String tenant) {
        MultiTenantRepository.currentTenant.set(tenant);
    }

    static String getCurrentTenant() {
        return currentTenant.get();
    }

    static void clear() {
        currentTenant.remove();
    }

    default <T> Map<String, T> forAll(Stream<String> tenants, Function<Self, T> function) {
        final String currentDB = getCurrentTenant();
        try {
            final Map<String, T> resultMap = tenants.map(s -> {
                setCurrentDb(s);
                final T result = function.apply((Self) this);
                return Map.entry(s, result);
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return resultMap;
        } finally {
            setCurrentDb(currentDB);
        }
    }

    default void forAll(Stream<String> tenants, Consumer<Self> function) {
        final String currentDB = getCurrentTenant();
        try {
            tenants.forEach(s -> {
                setCurrentDb(s);
                function.accept((Self) this);
            });
        } finally {
            setCurrentDb(currentDB);
        }
    }

    default Scope openScope(String scope) {
        return new Scope(scope);
    }

    class Scope implements AutoCloseable {

        private final String parentScope;

        private Scope(String scope) {
            parentScope = getCurrentTenant();
            setCurrentDb(scope);
        }

        @Override
        public void close() {
            setCurrentDb(parentScope);
        }
    }

}

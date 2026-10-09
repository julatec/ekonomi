package name.julatec.ekonomi.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoRepositoryBean
public interface MultiTenantRepository<T, ID, Self extends MultiTenantRepository<T, ID, Self>>
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

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

    // Se llama `mapAll` y no `forAll` para no solaparse con la variante de `Consumer`: con
    // lambdas, `forAll(tenants, r -> r.save(x))` encaja en las dos y el compilador lo rechaza
    // por ambiguo. Esta variante no tenía ningún llamador cuando se renombró.
    default <T> Map<String, T> mapAll(Stream<String> tenants, Function<Self, T> function) {
        final String currentDB = getCurrentTenant();
        try {
            final Map<String, T> resultMap = tenants.map(s -> {
                setCurrentDb(s);
                // `Self extends MultiTenantRepository<..., Self>` obliga a que `this` sea un Self;
                // javac no puede demostrarlo (idioma de «tipo propio»).
                @SuppressWarnings("unchecked") final Self self = (Self) this;
                final T result = function.apply(self);
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
                @SuppressWarnings("unchecked") final Self self = (Self) this;
                function.accept(self);
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

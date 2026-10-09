package name.julatec.ekonomi.extract.command;

import org.slf4j.Logger;

import java.util.Optional;
import java.util.TreeMap;

public class Context<T extends BaseCommand<T>> {

    public final Context<?> parentContext;
    public final Logger logger;
    private final TreeMap<String, Object> attributes = new TreeMap<>();

    public Context(Context<?> parentContext, Logger logger) {
        this.parentContext = parentContext;
        this.logger = logger;
    }

    // El cast no se puede verificar: `attributes` guarda cualquier cosa bajo clave de texto y
    // quien llama decide el tipo `V` que espera, igual que un `Map<String, Object>` con acceso
    // tipado. Es el contrato de este contenedor, no un descuido; si el tipo no coincide falla
    // en el sitio de uso con ClassCastException, que es donde se puede diagnosticar.
    @SuppressWarnings("unchecked")
    public <V> Optional<V> getAttribute(String attribute) {
        if (attributes.containsKey(attribute)) {
            return Optional.ofNullable((V) attributes.get(attribute));
        }
        if (parentContext != null) {
            return parentContext.getAttribute(attribute);
        }
        return Optional.empty();
    }

    public <V> void setAttribute(String key, V value) {
        this.attributes.put(key, value);
    }

    public <T extends BaseCommand<T>> Context<T> push() {
        return new Context<T>(this, logger);
    }

}

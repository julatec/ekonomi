package name.julatec.ekonomi.extract.command;

import org.slf4j.Logger;

import java.util.Optional;
import java.util.TreeMap;

public class Context<T extends BaseCommand<T>> {

    public final Context<? extends BaseCommand> parentContext;
    public final Logger logger;
    private final TreeMap<String, Object> attributes = new TreeMap<>();

    public Context(Context<?> parentContext, Logger logger) {
        this.parentContext = parentContext;
        this.logger = logger;
    }

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

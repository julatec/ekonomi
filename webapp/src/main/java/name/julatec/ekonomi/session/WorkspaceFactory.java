package name.julatec.ekonomi.session;

import name.julatec.ekonomi.security.User;
import name.julatec.ekonomi.storage.MultiTenantRepository;
import name.julatec.ekonomi.tribunet.storage.FacturaRepository;
import name.julatec.util.algebraic.Lattice;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class WorkspaceFactory {

    final Map<String, Workspace> workspaceMap = new ConcurrentSkipListMap<>();

    private FacturaRepository facturaRepository;

    private ApplicationContext applicationContext;

    private static class NameCollector<R> implements Collector<
            Map.Entry<String, Pair<BigDecimal, String>>,
            TreeMap<String, Pair<BigDecimal, String>>,
            R> {

        private final Function<TreeMap<String, Pair<BigDecimal, String>>, R> finisher;

        private static final Lattice<Pair<BigDecimal, String>> pairLattice = Pair::compareTo;

        private NameCollector(Function<TreeMap<String, Pair<BigDecimal, String>>, R> finisher) {
            this.finisher = finisher;
        }

        @Override
        public Supplier<TreeMap<String, Pair<BigDecimal, String>>> supplier() {
            return TreeMap::new;
        }

        @Override
        public BiConsumer<TreeMap<String, Pair<BigDecimal, String>>, Map.Entry<String, Pair<BigDecimal, String>>> accumulator() {
            return (treeMap, stringPairEntry) ->
                    treeMap.merge(stringPairEntry.getKey(), stringPairEntry.getValue(), pairLattice::max);
        }

        @Override
        public BinaryOperator<TreeMap<String, Pair<BigDecimal, String>>> combiner() {
            return (treeMap, treeMap2) -> {
                treeMap2.forEach((k, v) -> treeMap.merge(k, v, pairLattice::max));
                return treeMap;
            };
        }

        @Override
        public Function<TreeMap<String, Pair<BigDecimal, String>>, R> finisher() {
            return finisher;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }


    synchronized Workspace getWorkspace(Authentication authentication, HttpServletRequest request) {

        final User user = (User) authentication.getPrincipal();

        if (workspaceMap.containsKey(user.getUsername())) {
            Workspace workspace = workspaceMap
                    .get(user.getUsername())
                    .setRequest(request, this::getSessionClients);
            return workspace;
        }

        final Workspace workspace = applicationContext.getBean(Workspace.class, user);
        workspaceMap.put(user.getUsername(), workspace);



        if (MultiTenantRepository.getCurrentTenant() == null) {
            boolean selected = false;
            for (Cookie cookie : request.getCookies()) {
                switch (cookie.getName()) {
                    case "tenant":
                        workspace.setTargetPersistanceUnit(cookie.getValue());
                        MultiTenantRepository.setCurrentDb(cookie.getValue());
                        selected = true;
                        break;
                    default:
                        break;
                }
                if (selected) {
                    break;
                }
            }
            if (selected == false) {
                final String firstDb = user.getDatasources().iterator().next();
                MultiTenantRepository.setCurrentDb(firstDb);
                workspace.setTargetPersistanceUnit(firstDb);
                workspace.setClients(getSessionClients());
            }
        }

        workspace.setClients(getSessionClients());

        return workspaceMap.get(user.getUsername())
                .setRequest(request, this::getSessionClients);
    }

    private TreeSet<Session.Client> getSessionClients() {
        return facturaRepository
                .getClients()
                .stream()
                .map(this::extractNameComponents)
                .collect(new NameCollector<>(this::toSesssionClients));
    }

    protected TreeSet<Session.Client> toSesssionClients(TreeMap<String, Pair<BigDecimal, String>> treeMap) {
        return treeMap
                .entrySet()
                .stream()
                .map(kv -> new Session.Client()
                        .setName(kv.getValue().getValue())
                        .setId(kv.getKey())
                        .setRecords(kv.getValue().getLeft()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private Map.Entry<String, Pair<BigDecimal, String>> extractNameComponents(Object[] clients) {
        String name = Arrays.stream(String.valueOf(clients[2]).split(" "))
                .filter(StringUtils::isNotBlank)
                .map(input -> input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
        String numero = (String) clients[0];
        BigDecimal records = (BigDecimal) clients[1];
        return Map.entry(numero, Pair.of(records, name));
    }

    @Autowired
    public WorkspaceFactory setFacturaRepository(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
        return this;
    }

    @Autowired
    public WorkspaceFactory setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return this;
    }
}

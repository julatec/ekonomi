package name.julatec.ekonomi.session;

import name.julatec.ekonomi.AppConfig;
import name.julatec.ekonomi.security.ImportBankTransaction;
import name.julatec.ekonomi.security.ImportTransaction;
import name.julatec.ekonomi.security.Inbox;
import name.julatec.ekonomi.security.User;
import name.julatec.ekonomi.storage.MultiTenantRepository;
import name.julatec.util.algebraic.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class Workspace {

    public static final String Interval_LOWER_COOKIE = "rangeLower";
    public static final String Interval_UPPER_COOKIE = "rangeUpper";
    public static final String TENANT_COOKIE = "tenant";

    ResourceBundleMessageSource messages;

    final User user;
    private Locale locale;
    private SortedSet<Session.Client> clients = new TreeSet<>();
    private Interval<Date> dateInterval = getDefaultDateInterval();
    private Map<UUID, ImportTransaction<?>> importTransactionMap;
    private Map<String, Set<String>> importTransactionMapByOwnerId = new HashMap<>();
    private String targetPersistanceUnit;

    public static Interval<Date> getDefaultDateInterval() {
        final Date today = new Date();
        // convert date to calendar
        final Calendar c = Calendar.getInstance();
        c.setTime(today);
        c.add(Calendar.MONTH, -3);
        final Date start = c.getTime();
        return Interval.of(start, today);
    }

    public String getTargetPersistanceUnit() {
        return targetPersistanceUnit;
    }

    public Workspace setTargetPersistanceUnit(String targetPersistanceUnit) {
        this.targetPersistanceUnit = targetPersistanceUnit;
        return this;
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public Workspace(User user) {
        this.user = user;
        this.importTransactionMap = user.getImportBankAccounts()
                .stream()
                .collect(Collectors.toMap(ImportBankTransaction::getAccount, importBankAccount -> importBankAccount));
        user.getImportManualTransactions()
                .forEach(transaction -> this.importTransactionMap.put(transaction.getAccount(), transaction));
        user.getImportBankOperations()
                .forEach(transaction -> this.importTransactionMap.put(transaction.getAccount(), transaction));
        this.importTransactionMap.values().forEach(
                importTransaction -> {
                    importTransactionMapByOwnerId.putIfAbsent(importTransaction.getOwnerId(), new TreeSet<>());
                    importTransactionMapByOwnerId.get(importTransaction.getOwnerId()).add(importTransaction.getPersistenceUnit());
                });
    }

    private static Optional<Date> getDateFromIsoCookie(HttpServletRequest request, String cookieName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return Optional.ofNullable(WebUtils.getCookie(request, cookieName))
                .map(Cookie::getValue)
                .map(v -> v.substring(0, 10))
                .flatMap(source -> {
                    try {
                        return Optional.of(dateFormat.parse(source));
                    } catch (ParseException e) {
                        return Optional.empty();
                    }
                });
    }

    private static String getTenantFromCookie(
            final HttpServletRequest request,
            final String cookieName,
            final String defaultValue) {
        return Optional.ofNullable(WebUtils.getCookie(request, cookieName))
                .map(Cookie::getValue)
                .or(() -> Optional.ofNullable(defaultValue))
                .map(tenant -> {
                    MultiTenantRepository.setCurrentDb(tenant);
                    return tenant;
                }).get();
    }

    public SortedSet<Session.Client> getClients() {
        return clients;
    }

    public ImportTransaction<?> getImportBankAccount(UUID uuid) {
        return this.importTransactionMap.get(uuid);
    }

    public Workspace setClients(SortedSet<Session.Client> clients) {
        this.clients = clients;
        return this;
    }

    public Interval<Date> getDateInterval() {
        return dateInterval;
    }

    Session getSession() {
        return new Session()
                .setUsername(user.getDisplayName())
                .setClients(clients)
                .setLowerDate(dateInterval.lower)
                .setUpperDate(dateInterval.upper)
                .setTenants(new TreeSet<>(user.getDatasources()))
                .setTenant(targetPersistanceUnit)
                .setInboxes(user.getInboxes()
                        .stream()
                        .map(Inbox::getEmail)
                        .collect(toCollection(TreeSet::new)))
                .setImportAccounts(
                        this.importTransactionMap
                                .values()
                                .stream()
                                .map(Session.ImportAccount::of)
                                .collect(toCollection(TreeSet::new)));
    }

    public Workspace setRequest(HttpServletRequest request, Supplier<? extends SortedSet<Session.Client>> clients) {
        final Interval<Date> dateInterval = this.dateInterval;
        final Date lower = getDateFromIsoCookie(request, Interval_LOWER_COOKIE).orElse(dateInterval.lower);
        final Date upper = getDateFromIsoCookie(request, Interval_UPPER_COOKIE).orElse(dateInterval.upper);
        final String tenant = getTenantFromCookie(
                request,
                TENANT_COOKIE,
                user.getDatasources().iterator().next());
        this.dateInterval = Interval.of(lower, upper);
        this.locale = request.getLocale();
        if (this.targetPersistanceUnit == null || !this.targetPersistanceUnit.equals(tenant)) {
            setTargetPersistanceUnit(tenant);
            this.clients = clients.get();
        }
        return this;
    }

    public Set<String> getBankPersistenceUnits(String id) {
        return importTransactionMapByOwnerId.getOrDefault(id, Collections.emptySet());
    }

    public <T extends Enum<T>> String getLocalizedMessage(T template, Object... args) {
        final String templateName = String.format("%s.%s", template.getClass().getName(), template.name());
        return messages.getMessage(templateName, args, locale);
    }

    @Autowired
    Workspace setMessages(
            @Qualifier(AppConfig.MESSAGES) ResourceBundleMessageSource messages) {
        this.messages = messages;
        return this;
    }
}

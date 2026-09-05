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

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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

    /**
     * Determina el tenant activo a partir de la cookie del cliente.
     * <p>
     * La cookie la controla quien hace la petición, así que su valor solo se
     * acepta si está entre los datasources asignados al usuario. Sin ese filtro,
     * cambiar la cookie bastaba para leer la contabilidad de cualquier otro
     * tenant. Un valor ajeno o vencido cae al predeterminado en vez de fallar,
     * para que una cookie vieja no rompa la sesión.
     */
    private static String getTenantFromCookie(
            final HttpServletRequest request,
            final String cookieName,
            final Set<String> allowedTenants,
            final String defaultValue) {
        return Optional.ofNullable(WebUtils.getCookie(request, cookieName))
                .map(Cookie::getValue)
                .filter(allowedTenants::contains)
                .orElse(defaultValue);
    }

    public ImportTransaction<?> getImportBankAccount(UUID uuid) {
        return this.importTransactionMap.get(uuid);
    }

    public Interval<Date> getDateInterval() {
        return dateInterval;
    }

    public Session getSession() {
        return new Session()
                .setUsername(user.getDisplayName())
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

    public Workspace setRequest(HttpServletRequest request) {
        final Interval<Date> dateInterval = this.dateInterval;
        final Date lower = getDateFromIsoCookie(request, Interval_LOWER_COOKIE).orElse(dateInterval.lower);
        final Date upper = getDateFromIsoCookie(request, Interval_UPPER_COOKIE).orElse(dateInterval.upper);
        final String tenant = getTenantFromCookie(
                request,
                TENANT_COOKIE,
                user.getDatasources(),
                user.getDatasources().iterator().next());
        // El tenant vive en un ThreadLocal y Tomcat reutiliza los hilos, así que
        // hay que fijarlo en cada petición, no solo cuando cambia.
        MultiTenantRepository.setCurrentDb(tenant);
        this.dateInterval = Interval.of(lower, upper);
        this.locale = request.getLocale();
        if (this.targetPersistanceUnit == null || !this.targetPersistanceUnit.equals(tenant)) {
            setTargetPersistanceUnit(tenant);
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

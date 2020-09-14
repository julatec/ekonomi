package name.julatec.ekonomi.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity(name = "user")
public class User implements UserDetails {

    @Id
    private String username;

    private String displayName;

    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<UserId> ids;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> datasources;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_inbox",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "email"))
    private Set<Inbox> inboxes;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_import_bank_account",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "account"))
    private Set<ImportBankTransaction> importBankAccounts;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_import_manual_account",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "account"))
    private Set<ImportManualTransaction> importManualTransactions;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_import_operation_account",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "account"))
    private Set<ImportBankOperation> importBankOperations;

    @Transient
    private Set<SimpleGrantedAuthority> grantedAuthorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (grantedAuthorities == null || grantedAuthorities.isEmpty()) {
            setRoles(this.roles);
        }
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public User setRoles(Set<String> roles) {
        this.roles = roles;
        this.grantedAuthorities = new HashSet<>();
        for (String role : roles) {
            final SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role);
            this.grantedAuthorities.add(simpleGrantedAuthority);
        }
        this.grantedAuthorities = Collections.unmodifiableSet(this.grantedAuthorities);
        return this;
    }

    public Set<UserId> getIds() {
        return ids;
    }

    public User setIds(Set<UserId> userIds) {
        this.ids = userIds;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getDisplayName() {
        return displayName;
    }

    public User setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public Set<Inbox> getInboxes() {
        return inboxes;
    }

    public User setInboxes(Set<Inbox> inboxes) {
        this.inboxes = inboxes;
        return this;
    }

    public Set<String> getDatasources() {
        return datasources;
    }

    public void setDatasources(Set<String> persistanceUnits) {
        this.datasources = persistanceUnits;
    }

    public Set<ImportBankTransaction> getImportBankAccounts() {
        return importBankAccounts;
    }

    public Set<ImportManualTransaction> getImportManualTransactions() {
        return importManualTransactions;
    }

    public Set<ImportBankOperation> getImportBankOperations() {
        return importBankOperations;
    }

}

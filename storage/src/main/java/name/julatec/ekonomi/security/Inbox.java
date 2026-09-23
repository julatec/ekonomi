package name.julatec.ekonomi.security;


import org.springframework.data.annotation.Transient;

import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.persistence.*;
import java.util.Properties;
import java.util.Set;

import static jakarta.mail.Session.getInstance;

@Entity(name = "inbox")
public class Inbox {

    @Transient
    private transient final Authenticator authenticator = new Authenticator();
    @Id
    private String email;
    private String hostname;
    private String password;
    private String transportProtocol;
    private boolean tls;
    private String storeProtocol;
    @ManyToMany(mappedBy = "inboxes")
    private Set<User> users;
    private boolean active = false;
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> datasources;

    public jakarta.mail.Session getSession() {
        final Properties properties = new Properties();
        properties.setProperty("mail.host", hostname);
        properties.setProperty("mail.transport.protocol", transportProtocol);
        properties.setProperty("mail.imaps.host", hostname);
        properties.setProperty("mail.imaps.starttls.enable", String.valueOf(tls));
        properties.setProperty("mail.store.protocol", storeProtocol);
        Session session = getInstance(properties, authenticator);
        return session;
    }

    public String getEmail() {
        return email;
    }

    public Inbox setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getHostname() {
        return hostname;
    }

    public Inbox setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Inbox setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getTransportProtocol() {
        return transportProtocol;
    }

    public Inbox setTransportProtocol(String transportProtocol) {
        this.transportProtocol = transportProtocol;
        return this;
    }

    public boolean isTls() {
        return tls;
    }

    public Inbox setTls(boolean tls) {
        this.tls = tls;
        return this;
    }

    public String getStoreProtocol() {
        return storeProtocol;
    }

    public Inbox setStoreProtocol(String storeProtocol) {
        this.storeProtocol = storeProtocol;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public Inbox setActive(boolean active) {
        this.active = active;
        return this;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Inbox setUsers(Set<User> users) {
        this.users = users;
        return this;
    }

    public Set<String> getDatasources() {
        return datasources;
    }

    public void setDatasources(Set<String> persistanceUnits) {
        this.datasources = persistanceUnits;
    }

    /**
     * Acá había, comentada, una llamada a {@code decrypt(password)}.
     * <p>
     * Se quitó el 22 set 2026 junto con los métodos {@code encrypt}/{@code decrypt} que la
     * acompañaban. Cifraban con una llave y un IV <b>escritos en esta misma clase</b>
     * ({@code "aesEncryptionKey"} / {@code "encryptionIntVec"}, las cadenas de ejemplo de un
     * fragmento muy copiado), en un repositorio <b>público</b>. Una llave publicada no
     * protege de nada: lo único que aportaba era parecer que sí.
     * <p>
     * No estaban en uso —ninguna llamada en todo el repositorio— y las contraseñas guardadas
     * están en claro, comprobado por su largo: AES/CBC con relleno daría 24 caracteres Base64
     * para cualquiera de ellas, y miden 8, 8, 19 y 20.
     * <p>
     * <b>El problema de fondo sigue abierto</b>, y borrar esto no lo toca: la contraseña vive
     * en claro en la columna {@code inbox.password}, o sea también en cada respaldo de la
     * base. El arreglo es sacarla a {@code /etc/tomcat/secrets.env}, que es el patrón que esta
     * aplicación ya usa para todos los demás secretos y que no inventa un manejo de llaves
     * nuevo.
     */
    private class Authenticator extends jakarta.mail.Authenticator {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(email, password);
        }
    }

}

package name.julatec.ekonomi.security;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.data.annotation.Transient;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Set;

import static javax.mail.Session.getInstance;

@Entity(name = "inbox")
public class Inbox {

    private static final String key = "aesEncryptionKey";
    private static final String initVector = "encryptionIntVec";
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

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public javax.mail.Session getSession() {
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

    private class Authenticator extends javax.mail.Authenticator {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            //return new PasswordAuthentication(email, decrypt(password));
            return new PasswordAuthentication(email, password);
        }
    }

}

# SSL/TLS Certificate Renewal — ekonomi.julatec.name

**Date:** 2026-02-21
**Performed by:** julatec
**Server:** Apache Tomcat 11.0.6 on /opt/tomcat
**Hostname:** ekonomi.julatec.name

---

## Context

The SSL/TLS certificate for `ekonomi.julatec.name` expired on **February 21, 2026**.
A new certificate was obtained from **Sectigo** (certificate order `2342518739`) and installed
on the same day using the existing private key in the JKS keystore.

---

## Certificate Details

| Field | Old Certificate | New Certificate |
|---|---|---|
| **Subject** | CN=ekonomi.julatec.name | CN=ekonomi.julatec.name |
| **Issuer** | Sectigo RSA Domain Validation Secure Server CA | Sectigo Public Server Authentication CA DV R36 |
| **Valid From** | Jan 21, 2025 | Feb 21, 2026 |
| **Valid Until** | **Feb 21, 2026 (expired)** | **Mar 24, 2027** |
| **Key** | RSA (reused — no new CSR required) | RSA (same private key) |
| **Keystore alias** | server | server |

---

## Files Involved

| File | Location | Purpose |
|---|---|---|
| `ekonomi.julatec.name.jks` | `/opt/tomcat/conf/` | Server keystore (private key + cert chain) |
| `ekonomi.julatec.name.jks.bak-20260221` | `/opt/tomcat/conf/` | Pre-renewal backup |
| `ekonomi_julatec_name.crt` | `/opt/tomcat/conf/` | New end-entity certificate |
| `SectigoPublicServerAuthenticationCADVR36.crt` | `/opt/tomcat/conf/` | New intermediate CA |
| `SectigoPublicServerAuthenticationRootR46_USERTrust.crt` | `/opt/tomcat/conf/` | New intermediate CA |
| `USERTrustRSACertificationAuthority.crt` | `/opt/tomcat/conf/` | New root CA |
| `My_CA_Bundle.ca-bundle` | `/opt/tomcat/conf/` | Combined CA bundle |
| `2342518739.zip` | `/tmp/` | Original certificate bundle from Sectigo |

---

## New Certificate Chain

```
ekonomi.julatec.name
  └── Sectigo Public Server Authentication CA DV R36
        └── Sectigo Public Server Authentication Root R46 (cross-signed by USERTrust)
              └── USERTrust RSA Certification Authority
```

---

## Steps Performed

### 1. Extract certificate bundle

```bash
mkdir -p /tmp/cert-2026
unzip /tmp/2342518739.zip -d /tmp/cert-2026/

# Fix permissions (Windows zip format strips execute bit from directories)
chmod 755 "/tmp/cert-2026/CER - CRT Files" "/tmp/cert-2026/Plain Text Files"
chmod 644 "/tmp/cert-2026/CER - CRT Files/"*
```

### 2. Verify new end-entity certificate

```bash
openssl x509 -in "/tmp/cert-2026/CER - CRT Files/ekonomi_julatec_name.crt" \
  -noout -subject -issuer -dates
```

**Output:**
```
subject=CN = ekonomi.julatec.name
issuer=CN=Sectigo Public Server Authentication CA DV R36, O=Sectigo Limited, C=GB
notBefore=Feb 21 00:00:00 2026 GMT
notAfter=Mar 24 23:59:59 2027 GMT
```

### 3. Backup existing keystore

```bash
sudo cp /opt/tomcat/conf/ekonomi.julatec.name.jks \
        /opt/tomcat/conf/ekonomi.julatec.name.jks.bak-20260221
```

### 4. Import new CA certificates into keystore

The new Sectigo chain uses different CA certificates than the previous chain
(R36/R46 series instead of the older RSA DV / USERTRUST AAA chain).
Three CA certs were added as trusted entries.

```bash
KS=/opt/tomcat/conf/ekonomi.julatec.name.jks
KS_PASS="<keystore password>"
CERTS="/tmp/cert-2026/CER - CRT Files"

# Root CA
sudo /opt/jdk-21.0.2+13/bin/keytool -import -trustcacerts \
  -alias usertrust-rsa-ca \
  -file "${CERTS}/USERTrustRSACertificationAuthority.crt" \
  -keystore "$KS" -storepass "$KS_PASS" -noprompt

# Sectigo Root R46
sudo /opt/jdk-21.0.2+13/bin/keytool -import -trustcacerts \
  -alias sectigo-root-r46 \
  -file "${CERTS}/SectigoPublicServerAuthenticationRootR46_USERTrust.crt" \
  -keystore "$KS" -storepass "$KS_PASS" -noprompt

# Sectigo DV R36 (intermediate, direct issuer)
sudo /opt/jdk-21.0.2+13/bin/keytool -import -trustcacerts \
  -alias sectigo-dv-r36 \
  -file "${CERTS}/SectigoPublicServerAuthenticationCADVR36.crt" \
  -keystore "$KS" -storepass "$KS_PASS" -noprompt
```

### 5. Import new end-entity certificate to `server` alias

```bash
sudo /opt/jdk-21.0.2+13/bin/keytool -import \
  -alias server \
  -file "${CERTS}/ekonomi_julatec_name.crt" \
  -keystore "$KS" -storepass "$KS_PASS"
```

**Output:** `Certificate reply was installed in keystore`

### 6. Verify updated keystore

```bash
sudo /opt/jdk-21.0.2+13/bin/keytool -list -v \
  -keystore "$KS" -storepass "$KS_PASS" \
  | grep -A30 "Alias name: server" | grep -E "(Valid|Owner|Issuer|chain)"
```

**Output:**
```
Certificate chain length: 5
Owner: CN=ekonomi.julatec.name
Issuer: CN=Sectigo Public Server Authentication CA DV R36 ...
Valid from: Fri Feb 20 18:00:00 CST 2026 until: Wed Mar 24 17:59:59 CST 2027
```

### 7. Copy new certificate files to Tomcat conf directory

```bash
sudo cp "${CERTS}/ekonomi_julatec_name.crt"                               /opt/tomcat/conf/
sudo cp "${CERTS}/SectigoPublicServerAuthenticationCADVR36.crt"           /opt/tomcat/conf/
sudo cp "${CERTS}/SectigoPublicServerAuthenticationRootR46_USERTrust.crt" /opt/tomcat/conf/
sudo cp "${CERTS}/USERTrustRSACertificationAuthority.crt"                 /opt/tomcat/conf/
sudo cp "${CERTS}/My_CA_Bundle.ca-bundle"                                 /opt/tomcat/conf/
```

### 8. Restart Tomcat

```bash
sudo -u tomcat /opt/tomcat/bin/shutdown.sh
sleep 6
sudo -u tomcat /opt/tomcat/bin/startup.sh
```

### 9. Verify certificate live on port 8443

```bash
openssl s_client -connect localhost:8443 -servername ekonomi.julatec.name \
  </dev/null 2>/dev/null | openssl x509 -noout -subject -issuer -dates
```

**Output:**
```
subject=CN = ekonomi.julatec.name
issuer=C = GB, O = Sectigo Limited, CN = Sectigo Public Server Authentication CA DV R36
notBefore=Feb 21 00:00:00 2026 GMT
notAfter=Mar 24 23:59:59 2027 GMT
```

---

## No Configuration Changes Required

`server.xml` was **not modified**. It already points to the correct keystore:

```xml
keystoreFile="conf/ekonomi.julatec.name.jks"
keystorePassword="..."
```

The `firma-digital-rsa.jks` truststore (mTLS client CAs for Costa Rican digital signatures)
was also **not modified**.

---

## Notes

- **Private key was reused** — no new CSR was needed; only the certificate was replaced.
- **Chain changed:** new Sectigo R36/R46 chain replaces old RSA DV / USERTrust AAA chain.
  Three new CA aliases added to keystore: `usertrust-rsa-ca`, `sectigo-root-r46`, `sectigo-dv-r36`.
- **Next renewal:** approximately **January 2027** (before Mar 24, 2027 expiry).
- No automated renewal is configured — set a calendar reminder.

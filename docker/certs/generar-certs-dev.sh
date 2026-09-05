#!/usr/bin/env bash
#
# Material criptográfico del ambiente LOCAL. Nada de esto sirve fuera de esta máquina.
#
# Por qué existe: la aplicación autentica exclusivamente por certificado X.509
# (`anyRequest().authenticated()` + `.x509(...)`), así que sin un certificado de cliente no
# se llega ni al 401 — no hay forma de abrir la UI en el navegador para iterar.
#
# Por qué NO se usa la CA interna del proyecto `certificados-internos`: su raíz todavía no
# existe. Crearla exige elegir una frase de paso y decidir dónde vive la llave, que es una
# decisión de la persona, no de un script. Cuando esa CA esté operativa, este material se
# puede tirar y usar aquélla.
#
# 🔴 Esta CA NO debe entrar nunca al truststore de la firma digital. Ekonomi resuelve el
# emisor comparando el CN como string, sin mirar huella ni serial: si comparten truststore,
# quien tenga esta llave entra como cualquier cédula registrada.
set -euo pipefail

cd "$(dirname "$0")"

CA_CN="Ekonomi Dev CA"
CLIENT_CN="dev"
PASS="changeit"          # protege material autofirmado y desechable, en una carpeta gitignored
DIAS=825                 # tope que aceptan los navegadores para certificados de servidor

rm -f ./*.pem ./*.p12 ./*.srl ./*.csr ./*.cnf

echo "1/4  CA de desarrollo"
openssl req -x509 -newkey rsa:2048 -sha256 -days 3650 -nodes \
  -keyout dev-ca.key.pem -out dev-ca.crt.pem \
  -subj "/CN=${CA_CN}" \
  -addext "basicConstraints=critical,CA:TRUE,pathlen:0" \
  -addext "keyUsage=critical,keyCertSign,cRLSign" 2>/dev/null

echo "2/4  certificado del servidor (localhost)"
cat > server.cnf <<'EOF'
[req]
distinguished_name = dn
[dn]
[ext]
basicConstraints = CA:FALSE
keyUsage = critical,digitalSignature,keyEncipherment
extendedKeyUsage = serverAuth
subjectAltName = DNS:localhost,IP:127.0.0.1
EOF
openssl req -newkey rsa:2048 -nodes -keyout server.key.pem -out server.csr \
  -subj "/CN=localhost" 2>/dev/null
openssl x509 -req -in server.csr -CA dev-ca.crt.pem -CAkey dev-ca.key.pem -CAcreateserial \
  -out server.crt.pem -days "${DIAS}" -sha256 -extfile server.cnf -extensions ext 2>/dev/null
openssl pkcs12 -export -out localhost-server.p12 -inkey server.key.pem -in server.crt.pem \
  -name localhost -passout "pass:${PASS}"

echo "3/4  truststore con la CA (para exigir certificado de cliente)"
keytool -importcert -noprompt -alias dev-ca -file dev-ca.crt.pem \
  -keystore dev-truststore.p12 -storetype PKCS12 -storepass "${PASS}" 2>/dev/null

echo "4/4  certificado de cliente (CN=${CLIENT_CN})"
cat > client.cnf <<'EOF'
[req]
distinguished_name = dn
[dn]
[ext]
basicConstraints = CA:FALSE
keyUsage = critical,digitalSignature
extendedKeyUsage = clientAuth
EOF
openssl req -newkey rsa:2048 -nodes -keyout client.key.pem -out client.csr \
  -subj "/CN=${CLIENT_CN}" 2>/dev/null
openssl x509 -req -in client.csr -CA dev-ca.crt.pem -CAkey dev-ca.key.pem -CAcreateserial \
  -out client.crt.pem -days "${DIAS}" -sha256 -extfile client.cnf -extensions ext 2>/dev/null

# Dos envases del MISMO certificado, a propósito:
#   - moderno: para curl y para la JVM
#   - legacy (3DES/SHA-1): porque `security import` de macOS RECHAZA el envase de OpenSSL 3.5
#     con «MAC verification failed (wrong password?)» aunque la contraseña sea correcta. El
#     mensaje miente sobre la causa: lo que rechaza es el algoritmo.
openssl pkcs12 -export -out dev-client.p12 -inkey client.key.pem -in client.crt.pem \
  -certfile dev-ca.crt.pem -name "ekonomi-dev" -passout "pass:${PASS}"
openssl pkcs12 -export -legacy -out dev-client-compat.p12 -inkey client.key.pem -in client.crt.pem \
  -certfile dev-ca.crt.pem -name "ekonomi-dev" -passout "pass:${PASS}"

rm -f server.csr client.csr server.cnf client.cnf

echo
echo "Listo. Contraseña de todo: ${PASS}"
echo
echo "  Importar en el llavero del Mac (usar el envase compat):"
echo "    security import $(pwd)/dev-client-compat.p12 -k ~/Library/Keychains/login.keychain-db -P ${PASS}"
echo
echo "  Probar con curl:"
echo "    curl -sk --cert-type P12 --cert $(pwd)/dev-client.p12:${PASS} https://localhost:8443/api/session"

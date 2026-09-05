#!/usr/bin/env bash
#
# Trae de la CA interna (thot) el certificado del servidor local y la cadena de la CA.
#
# POR QUE ESTO EN VEZ DE LA CA DE DESARROLLO
#     Nada que instalar. `Julatec CA Raiz` ya esta confiada en el llavero de esta Mac
#     —se instalo para el certificado de cliente— asi que el navegador acepta el
#     servidor local sin que haya que agregar ninguna CA nueva ni correr sudo.
#
#     Con la CA de desarrollo habria que confiar en `Ekonomi Dev CA`, cuya llave privada
#     vive sin contrasena en esta misma carpeta. Confiarla es darle a ese archivo la
#     potestad de firmar cualquier nombre para este usuario.
#
# QUE HACE FALTA
#     Acceso ssh a thot con sudo. Si no lo tenes, `make dev-certs` deja el ambiente
#     funcionando igual con un certificado autofirmado — solo que el navegador va a
#     avisar, y hay que confiarlo a mano.
#
# EL NOMBRE NO ES DECORATIVO
#     El certificado cubre `ekonomi.promyse.home.julatec.name`, que resuelve a 127.0.0.1
#     SOLO por el /etc/hosts de esta maquina. Esa es la condicion bajo la cual la CA
#     interna emite certificados de servidor — ver el encabezado de su openssl.cnf. Si
#     algun dia ese nombre lo sirve el AdGuard de thot a la LAN, esto deja de valer.
set -euo pipefail

cd "$(dirname "$0")"

HOST_CA="${HOST_CA:-thot}"
NOMBRE="${NOMBRE:-ekonomi.promyse.home.julatec.name}"
PASS="changeit"
CA_REMOTA=/etc/ca-julatec
SALIDA_REMOTA=/var/lib/certificados

en_thot() { ssh -o BatchMode=yes "$HOST_CA" "sudo -n $*"; }

echo "1/3  cadena de la CA interna"
en_thot cat "$CA_REMOTA/raiz.crt"       > julatec-raiz.crt.pem
en_thot cat "$CA_REMOTA/intermedia.crt" > julatec-intermedia.crt.pem
for f in julatec-raiz julatec-intermedia; do
  printf '     %s\n' "$(openssl x509 -noout -subject -in "$f.crt.pem" | sed 's/^subject=//')"
done

echo "2/3  certificado del servidor para $NOMBRE"
# Se emite solo si no hay uno vigente con mas de 30 dias por delante: reemitir cada vez
# llenaria el indice de la CA de certificados que nadie uso.
vigente=0
if en_thot test -f "$SALIDA_REMOTA/$NOMBRE.p12" 2>/dev/null; then
  fin=$(en_thot openssl pkcs12 -in "$SALIDA_REMOTA/$NOMBRE.p12" -passin "pass:$PASS" -clcerts -nokeys 2>/dev/null \
        | openssl x509 -noout -enddate 2>/dev/null | cut -d= -f2 || true)
  if [ -n "$fin" ]; then
    dias=$(( ( $(date -j -f "%b %d %T %Y %Z" "$fin" +%s 2>/dev/null || echo 0) - $(date +%s) ) / 86400 ))
    [ "$dias" -gt 30 ] && { vigente=1; echo "     ya hay uno, vence en $dias dias"; }
  fi
fi
if [ "$vigente" -eq 0 ]; then
  echo "     emitiendo…"
  en_thot env CA_INTERMEDIA="$CA_REMOTA" SALIDA="$SALIDA_REMOTA" P12_PASS="$PASS" \
    /usr/local/sbin/emitir-servidor "$NOMBRE" localhost 127.0.0.1 >/dev/null
fi
en_thot cat "$SALIDA_REMOTA/$NOMBRE.p12" > servidor-local.p12
chmod 600 servidor-local.p12

echo "3/3  truststores de cliente"
# Las tres, a proposito: la interna para entrar vos desde el navegador con el certificado
# que ya tenes, y la de desarrollo porque el proxy de Vite y los scripts usan `CN=dev` y
# no pueden depender de un certificado que vence en 14 dias.
rm -f dev-truststore.p12
for par in "ekonomi-dev-ca:dev-ca.crt.pem" \
           "julatec-raiz:julatec-raiz.crt.pem" \
           "julatec-intermedia:julatec-intermedia.crt.pem"; do
  alias="${par%%:*}"; archivo="${par#*:}"
  [ -f "$archivo" ] || { echo "     (falta $archivo — corré primero make dev-certs)"; continue; }
  keytool -importcert -noprompt -alias "$alias" -file "$archivo" \
    -keystore dev-truststore.p12 -storetype PKCS12 -storepass "$PASS" 2>/dev/null
  echo "     + $alias"
done

# El truststore del 9443 del Tomcat de /opt/tomcat, con UNA sola CA.
#
# 🔴 Es un archivo APARTE del de la firma digital, y esa separacion es el punto entero.
# AuthenticationService resuelve el emisor comparando el STRING del CN, sin mirar huella
# ni serial: si esta CA entrara al truststore de la firma digital, quien tenga su llave
# podria firmar un intermedio con CN=CA SINPE - PERSONA FISICA y entrar como cualquier
# cedula registrada. Por eso la separacion es por PUERTO y por TRUSTSTORE, no por SNI
# —loadUserDetails nunca ve por cual SSLHostConfig entro la conexion—.
#
#   8443 -> firma-digital-local.p12  (las CAs del PKI nacional)
#   9443 -> ca-julatec-clientes.p12  (esta, con la raiz propia y nada mas)
rm -f ca-julatec-clientes.p12
keytool -importcert -noprompt -alias julatec-ca-raiz -file julatec-raiz.crt.pem \
  -keystore ca-julatec-clientes.p12 -storetype PKCS12 -storepass "$PASS" 2>/dev/null
chmod 600 ca-julatec-clientes.p12

cat <<FIN

  Listo. El servidor local presenta:
$(openssl pkcs12 -in servidor-local.p12 -nokeys -passin "pass:$PASS" 2>/dev/null \
   | openssl x509 -noout -subject -issuer -enddate -ext subjectAltName | sed 's/^/    /')

  Reiniciá la aplicación y abrí:
    https://$NOMBRE:8443/

  Para el Tomcat de /opt/tomcat (el que se pega a la contabilidad real), copiar
  tambien los dos archivos del conector 9443:

    cp servidor-local.p12 ca-julatec-clientes.p12 /opt/tomcat/conf/
FIN

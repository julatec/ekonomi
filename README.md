# Ekonomi

Support for collecting Factura Electronica from Multiple invoices and compile an auxiliar file.

## Desarrollo local

La aplicación autentica con certificado de cliente (firma digital) y no tiene usuario ni
contraseña. Sin certificado no se llega ni al 401, así que el primer paso siempre es
generarlo.

```bash
make dev-certs        # CA, certificado de servidor y .p12 de cliente en docker/certs/
make certs-thot       # opcional pero recomendado — ver abajo
make up               # MySQL en el 3307, MailHog y Adminer
make dev-seed         # los usuarios que corresponden a esos certificados
make build            # construye también el bundle de la interfaz
make run-local        # https://ekonomi.promyse.home.julatec.name:8443/
```

Y una línea en `/etc/hosts`, que necesita `sudo`:

```
127.0.0.1       ekonomi.promyse.home.julatec.name
```

### Por qué un nombre y no `localhost`

Con `localhost` el navegador **no muestra el diálogo del certificado y entra sin ninguno**,
que es lo más confuso que puede pasar: es un silencio, no un rechazo. Hay tres causas
posibles y conviene saber distinguirlas.

| Síntoma | Causa |
|---|---|
| No aparece ningún diálogo de certificado | El conector anuncia en el handshake **qué CAs acepta**, y el navegador solo ofrece certificados que encajen. Si la CA que emitió el tuyo no está en `dev-truststore.p12`, no hay nada que ofrecer |
| «La conexión no es privada» | El certificado del servidor lo firma una CA que el llavero no confía |
| Aviso de discordancia de nombre | El nombre por el que entrás no está en el SAN del certificado |

Para ver qué CAs anuncia el servidor:

```bash
openssl s_client -connect ekonomi.promyse.home.julatec.name:8443 </dev/null 2>&1 \
  | sed -n '/Acceptable client certificate CA names/,/Requested Signature/p'
```

Y para comprobar el certificado del servidor **con verificación de nombre** — `openssl
s_client` a secas NO comprueba el nombre, así que cualquier nombre «pasa» y la prueba no dice
nada:

```bash
security verify-cert -c <(openssl pkcs12 -in docker/certs/servidor-local.p12 -nokeys \
  -clcerts -passin pass:changeit) -p ssl -s ekonomi.promyse.home.julatec.name
```

### `make certs-thot` — el camino sin instalar nada

Trae de la CA interna (thot) el certificado del servidor y la cadena de la CA, y rearma el
truststore con **las tres** CAs de cliente. Después de esto:

- El navegador acepta el servidor **sin confiar nada nuevo**: `Julatec CA Raiz` ya está en el
  llavero de la Mac, puesta cuando se importó el certificado de firma.
- El navegador ofrece el certificado de persona que ya tenés (`CN=5-0359-0732`), porque el
  conector pasa a anunciar `Julatec CA Clientes`.
- El `CN=dev` de `make dev-certs` sigue sirviendo para curl y para el proxy de Vite.

Sin acceso a thot, `make dev-certs` deja todo funcionando igual con un certificado
autofirmado — solo que hay que confiarlo a mano y el navegador avisa hasta que lo hagas.

El puerto de MySQL es el **3307** a propósito. El 3306 lo suele ocupar un túnel SSH hacia la
base de producción, y con `localhost` resolviendo al túnel el perfil local terminaba apuntando
a la contabilidad real.

Con el perfil `local` se siembran comprobantes de ejemplo desde los recursos de prueba de
`tribunet-adapters`, para que el visualizador tenga algo que mostrar. Se cambia con
`name.julatec.ekonomi.local.comprobantes` y `name.julatec.ekonomi.local.tenant`.

### La interfaz

Vive en `webapp/frontend/` (React 18 + Vite) y la construye Maven en `generate-resources`: no
hay que compilarla a mano ni se versiona el bundle.

Para trabajar en ella con recarga en caliente:

```bash
cd webapp/frontend && npm run dev    # http://localhost:5173
```

El backend tiene que estar corriendo aparte en el 8443. El navegador entra por HTTP plano y es
el proxy de Vite el que presenta el certificado de cliente hacia el backend —un `fetch` no
puede hacer que el navegador vuelva a presentarlo—; el `.p12` se toma de `docker/certs/` y se
puede cambiar con `EKONOMI_DEV_P12` y `EKONOMI_DEV_P12_PASS`.

Dos detalles del dev server que cuestan una tarde si no se saben:

- Está atado a **127.0.0.1** y no al `localhost` por omisión. En macOS `localhost` resuelve
  primero a `::1`, así que Vite quedaba escuchando solo en `[::1]` — y un nombre del
  `/etc/hosts` apunta a `127.0.0.1`, donde no había nadie. No daba error de nombre ni 403:
  simplemente no conectaba.
- El nombre está en `server.allowedHosts`. Vite 6 rechaza con 403 toda petición cuyo `Host` no
  reconozca —es su protección contra DNS rebinding— y de fábrica solo trae `localhost`.

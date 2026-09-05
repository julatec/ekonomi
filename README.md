# Ekonomi

Support for collecting Factura Electronica from Multiple invoices and compile an auxiliar file.

## Desarrollo local

La aplicación autentica con certificado de cliente (firma digital) y no tiene usuario ni
contraseña. Sin certificado no se llega ni al 401, así que el primer paso siempre es
generarlo.

```bash
make dev-certs        # CA, certificado de servidor y .p12 de cliente en docker/certs/
make up               # MySQL en el 3307, MailHog y Adminer
make dev-seed         # el usuario que corresponde al certificado recién generado
make build            # construye también el bundle de la interfaz
make run-local        # https://localhost:8443/
```

Después hay que importar `docker/certs/dev-client.p12` en el navegador (la contraseña es
`changeit`). En macOS, Chrome y Safari rechazan el `.p12` moderno: para esos sirve
`dev-client-compat.p12`, que el mismo script genera con el cifrado viejo.

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

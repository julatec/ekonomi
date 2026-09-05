import { readFileSync } from 'node:fs'
import { Agent } from 'node:https'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// El backend exige certificado de cliente (firma digital), y el navegador no puede
// presentarlo contra el dev server de Vite: la conexión que lleva mTLS es la de Vite hacia
// Spring Boot, no la del navegador hacia Vite. Así que el proxy presenta el certificado de
// desarrollo por su cuenta y el navegador entra por HTTP plano a localhost:5173.
//
// Esto vale SOLO en desarrollo: en el WAR desplegado no hay proxy y cada navegador presenta
// su propio certificado. El .p12 está gitignorado y lo genera `make dev-certs`.
const RUTA_P12 = process.env.EKONOMI_DEV_P12
  || fileURLToPath(new URL('../../docker/certs/dev-client.p12', import.meta.url))
const CLAVE_P12 = process.env.EKONOMI_DEV_P12_PASS || 'changeit'

// El certificado va en un `Agent` propio y no suelto en las opciones del proxy: `http-proxy`
// solo reenvía `key`/`cert`/`pfx` cuando la conexión de entrada también es TLS, y acá la de
// entrada es HTTP plano. Con el agente explícito el material siempre viaja.
function agenteDeCliente() {
  try {
    return new Agent({
      pfx: readFileSync(RUTA_P12),
      passphrase: CLAVE_P12,
      // El certificado del servidor local es autofirmado.
      rejectUnauthorized: false,
      keepAlive: true,
    })
  } catch {
    // Sin certificado el dev server igual arranca: la interfaz se ve y las llamadas fallan
    // con 403, que es más útil que un error de arranque sin explicación.
    console.warn(`[ekonomi] sin certificado de cliente en ${RUTA_P12}; el API va a responder 403`)
    return undefined
  }
}

const AGENTE = agenteDeCliente()

// `base` distinto en build y en desarrollo, y en build absoluto y no relativo.
//
// En el WAR el mismo documento se sirve desde `/`, `/comprobantes` y `/comprobantes/{clave de
// 50 dígitos}`, porque el enrutador vive en el navegador. Con rutas relativas, cada nivel las
// resolvería contra un punto distinto: desde el detalle irían a buscar los assets a
// `/comprobantes/assets/…`. La aplicación se despliega como ROOT.war, así que el context path
// es `/`; si algún día dejara de serlo, esto hay que revisarlo.
//
// En desarrollo, en cambio, `base` tiene que ser `/`: Vite sirve el documento en `base`, y con
// `/dist/` la aplicación quedaría colgando de un prefijo que el enrutador no conoce.
//
// La salida va a una subcarpeta propia de `static/` y no a su raíz porque `emptyOutDir`
// borra el destino, y en la raíz se llevaría por delante lo que haya vendorizado ahí.
export default defineConfig(({ command }) => ({
  base: command === 'build' ? '/dist/' : '/',
  plugins: [react()],
  build: {
    outDir: '../src/main/resources/static/dist',
    emptyOutDir: true,
    sourcemap: true,
  },
  server: {
    port: 5173,
    // IPv4 explicito, y no el `localhost` por omision. En macOS `localhost` resuelve primero
    // a `::1`, asi que Vite quedaba escuchando SOLO en [::1] — y un nombre puesto en
    // /etc/hosts apunta a 127.0.0.1, donde no habia nadie: la conexion no daba error de
    // nombre ni 403, simplemente no conectaba. Atado a 127.0.0.1 sirve a los dos, porque
    // navegadores y curl caen a IPv4 cuando ::1 rechaza.
    host: '127.0.0.1',
    // Vite 6 rechaza con 403 toda peticion cuyo encabezado Host no reconozca —es su
    // proteccion contra DNS rebinding— y solo trae `localhost` y las IPs de fabrica. Sin
    // esta linea, abrir el dev server por el nombre del /etc/hosts da un "Blocked request"
    // que no se parece en nada a un problema de nombres.
    allowedHosts: ['ekonomi.promyse.home.julatec.name'],
    // En desarrollo el backend corre aparte. El proxy hace que todo viaje al mismo origen
    // desde el navegador, así CORS no entra en juego; `secure: false` es porque el
    // certificado del servidor local es autofirmado.
    proxy: Object.fromEntries(
      ['/api', '/report', '/upload', '/mcp'].map((ruta) => [
        ruta,
        {
          target: 'https://localhost:8443',
          changeOrigin: true,
          secure: false,
          agent: AGENTE,
        },
      ]),
    ),
  },
}))

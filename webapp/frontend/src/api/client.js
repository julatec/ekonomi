/**
 * Cliente HTTP de la API.
 *
 * Toda la aplicación vive detrás de mTLS: el certificado se presenta una sola vez, en el
 * handshake TLS, no por petición. Eso tiene una consecuencia que condiciona el manejo de
 * errores: **un `fetch` no puede pedirle al navegador que vuelva a presentar el certificado**.
 * Solo una navegación real dispara un handshake nuevo. Por eso un 401 no se maneja en el
 * componente que lo recibió —no hay nada que reintentar— sino que se avisa hacia arriba para
 * mostrar una pantalla que ofrezca recargar, que es la única salida real.
 */

/** Se dispara cuando la sesión dejó de ser válida. La escucha App para bloquear la pantalla. */
export const SESION_VENCIDA = 'ekonomi:sesion-vencida'

export class ErrorApi extends Error {
  constructor(status, mensaje) {
    super(mensaje)
    this.status = status
  }
}

async function mensajeDeError(respuesta) {
  // Spring devuelve JSON con `message` para los ResponseStatusException, pero la página de
  // error de Tomcat es HTML: intentar json() a ciegas escondería el error real detrás de un
  // fallo de parseo.
  const tipo = respuesta.headers.get('content-type') || ''
  if (tipo.includes('application/json')) {
    const cuerpo = await respuesta.json().catch(() => null)
    if (cuerpo?.message) return cuerpo.message
    if (cuerpo?.error) return cuerpo.error
  }
  return `La petición falló con ${respuesta.status}.`
}

export async function pedir(ruta, parametros) {
  const url = new URL(ruta, window.location.origin)
  Object.entries(parametros || {}).forEach(([clave, valor]) => {
    if (valor !== undefined && valor !== null && valor !== '') {
      url.searchParams.set(clave, valor)
    }
  })

  const respuesta = await fetch(url, {
    headers: { Accept: 'application/json' },
    credentials: 'same-origin',
  })

  if (respuesta.status === 401) {
    window.dispatchEvent(new CustomEvent(SESION_VENCIDA))
    throw new ErrorApi(401, 'El certificado ya no es válido para esta sesión.')
  }
  if (!respuesta.ok) {
    throw new ErrorApi(respuesta.status, await mensajeDeError(respuesta))
  }
  return respuesta.json()
}

/**
 * POST con cuerpo JSON. Existe solo para el asistente: todo lo demás de esta interfaz lee.
 *
 * Comparte con `pedir` el manejo del 401 —un `fetch` no puede hacer que el navegador vuelva a
 * presentar el certificado, así que se avisa hacia arriba— y la lectura del mensaje de error.
 */
export async function enviar(ruta, cuerpo) {
  const respuesta = await fetch(new URL(ruta, window.location.origin), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
    credentials: 'same-origin',
    body: JSON.stringify(cuerpo),
  })
  if (respuesta.status === 401) {
    window.dispatchEvent(new CustomEvent(SESION_VENCIDA))
    throw new ErrorApi(401, 'El certificado ya no es válido para esta sesión.')
  }
  if (!respuesta.ok) {
    throw new ErrorApi(respuesta.status, await mensajeDeError(respuesta))
  }
  return respuesta.json()
}

export const api = {
  sesion: () => pedir('/api/session'),
  clientes: (parametros) => pedir('/api/clients', parametros),
  comprobantes: (parametros) => pedir('/api/comprobantes', parametros),
  comprobante: (clave, parametros) => pedir(`/api/comprobantes/${encodeURIComponent(clave)}`, parametros),
  estadoDelChat: () => pedir('/api/chat'),
  preguntar: (mensaje, historial) => enviar('/api/chat', { mensaje, historial }),
  cabys: (parametros) => pedir('/api/cabys', parametros),
  cabysVersiones: () => pedir('/api/cabys/version'),
  actividades: (parametros) => pedir('/api/actividades', parametros),
}

/** La cookie que el backend ya usa para resolver el tenant y el rango de fechas. */
export function ponerCookie(nombre, valor) {
  document.cookie = `${nombre}=${encodeURIComponent(valor)}; path=/; SameSite=Lax`
}

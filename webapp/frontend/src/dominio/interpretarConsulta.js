/**
 * Decide qué se escribió en la caja de búsqueda.
 *
 * Una sola caja en vez de cinco campos, porque quien busca ya sabe qué tiene en la mano: pegó
 * una clave, un consecutivo, una cédula, o está escribiendo un nombre. Pedirle además que
 * elija en cuál campo va es trabajo que la interfaz puede hacer sola.
 *
 * Se clasifica por longitud, que es lo único confiable: la clave son 50 dígitos y el
 * consecutivo 20, ambos fijos por el esquema de Hacienda. La decisión siempre se muestra y
 * siempre se puede forzar a otro campo, porque adivinar mal y no dejar corregir sería peor que
 * no adivinar.
 *
 * Es una función pura a propósito: se puede probar sin montar React.
 */

export const CAMPOS = {
  clave: 'clave',
  consecutivo: 'consecutivo',
  cedula: 'cédula',
  nombre: 'nombre',
}

export function normalizar(texto) {
  return (texto || '').trim().replace(/[\s-]/g, '')
}

export function interpretarConsulta(texto) {
  const limpio = (texto || '').trim()
  if (!limpio) return { campo: null, valor: '' }

  const compacto = normalizar(limpio)

  if (/^\d{50}$/.test(compacto)) return { campo: 'clave', valor: compacto }
  if (/^\d{20}$/.test(compacto)) return { campo: 'consecutivo', valor: compacto }
  // Las cédulas costarricenses van de 9 (física) a 12 dígitos (jurídica y DIMEX).
  if (/^\d{9,12}$/.test(compacto)) return { campo: 'cedula', valor: compacto }

  return { campo: 'nombre', valor: limpio }
}

/**
 * Los parámetros que espera GET /api/comprobantes para el campo elegido.
 *
 * `lado` solo tiene sentido sobre una cédula, y traduce «venta» y «compra» a los filtros por
 * parte. La convención es la de los reportes que ya existen —`sales(numero)` busca por emisor,
 * `purchases(numero)` por receptor—, o sea: desde el punto de vista del número buscado. Si esa
 * contraparte emitió, para ella fue una venta; si recibió, una compra.
 */
export function comoParametros(campo, valor, lado) {
  if (!campo || !valor) return {}
  if (campo === 'cedula') {
    if (lado === 'venta') return { emisor: valor }
    if (lado === 'compra') return { receptor: valor }
  }
  return { [campo]: valor }
}

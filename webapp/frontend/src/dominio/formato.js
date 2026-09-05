import { tipo } from './tiposDeComprobante.js'

/**
 * El signo lo pone la interfaz, y tiene que salir de un solo lugar.
 *
 * El backend devuelve siempre el total en positivo: `ComprobanteResumen` copia
 * `totalComprobante` tal cual, y la negación de las notas de crédito existe únicamente dentro
 * de un `@Transient` del dominio que nunca sale. O sea: o la interfaz aplica el signo, o la
 * tabla miente sobre lo que suma y lo que resta.
 *
 * Está acá, compartido por la fila y por la banda de totales, porque si cada uno lo aplicara
 * por su cuenta llegaría el día en que uno de los dos se olvide.
 */
export function signoPorTipo(claveTipo) {
  return tipo(claveTipo).signo
}

export function conSigno(valor, claveTipo) {
  const numero = Number(valor ?? 0)
  return numero * signoPorTipo(claveTipo)
}

const formateadores = new Map()

function formateador(moneda) {
  const clave = moneda || 'CRC'
  if (!formateadores.has(clave)) {
    formateadores.set(
      clave,
      new Intl.NumberFormat('es-CR', {
        style: 'currency',
        currency: clave,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }),
    )
  }
  return formateadores.get(clave)
}

/**
 * Un `null` no es un cero: significa que el comprobante no traía resumen. Pintarlo como
 * `0,00` inventaría un dato; se muestra un guion.
 */
export function formatearMonto(valor, moneda) {
  if (valor === null || valor === undefined || valor === '') return '—'
  const numero = Number(valor)
  if (Number.isNaN(numero)) return '—'
  try {
    return formateador(moneda).format(numero)
  } catch {
    // Una moneda que Intl no conoce no debería tumbar la fila.
    return `${numero.toFixed(2)} ${moneda || ''}`.trim()
  }
}

export function formatearFecha(iso) {
  return iso || '—'
}

export function formatearEntero(valor) {
  if (valor === null || valor === undefined) return '—'
  return new Intl.NumberFormat('es-CR').format(Number(valor))
}

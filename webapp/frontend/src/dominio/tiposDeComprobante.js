/**
 * Los cinco tipos, con su etiqueta, su color y su signo.
 *
 * Es el contrato duro entre la tabla de resultados y el visualizador: si la etiqueta azul de
 * una fila abre una vista con encabezado de otro color, quien la mira pierde el hilo. Las
 * claves son exactamente las de `BusquedaComprobantes.TIPOS` en el backend, para no tener que
 * traducir entre dos vocabularios.
 *
 * El Recibo Electrónico de Pago (código 10 de la v4.4) no está: no existe clase adaptadora,
 * ni entidad, ni ingesta. No es un sexto tipo que falte pintar, es un proyecto aparte.
 */
export const TIPOS = {
  factura: { etiqueta: 'Factura', color: '#1d4ed8', fondo: '#dbeafe', signo: 1 },
  factura_compra: { etiqueta: 'Compra', color: '#6d28d9', fondo: '#ede9fe', signo: 1 },
  factura_exportacion: { etiqueta: 'Exportación', color: '#0f766e', fondo: '#ccfbf1', signo: 1 },
  nota_credito: { etiqueta: 'N. Crédito', color: '#b91c1c', fondo: '#fee2e2', signo: -1 },
  nota_debito: { etiqueta: 'N. Débito', color: '#b45309', fondo: '#fef3c7', signo: 1 },
}

export const ORDEN_TIPOS = [
  'factura',
  'factura_compra',
  'factura_exportacion',
  'nota_credito',
  'nota_debito',
]

export function tipo(clave) {
  return TIPOS[clave] || { etiqueta: clave, color: '#374151', fondo: '#f3f4f6', signo: 1 }
}

/**
 * Las dos notas ajustan otro comprobante, y el signo no sirve para distinguirlas: la de
 * débito suma igual que una factura. Lo que comparten es que sus totales no significan nada
 * sin saber qué documento corrigen, y por eso la referencia va antes de los totales en las
 * dos.
 */
export function esNotaDeAjuste(clave) {
  return clave === 'nota_credito' || clave === 'nota_debito'
}

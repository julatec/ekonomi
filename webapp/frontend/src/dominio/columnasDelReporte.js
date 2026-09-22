/**
 * Acomodar las columnas del reporte: mover, prender y apagar.
 *
 * Está acá y no dentro de la pantalla porque es lo único de esa pantalla que puede estar mal
 * sin que se note — una columna que se mueve a un lugar distinto del que se soltó, o una que
 * se pierde al reordenar, se ven bien hasta que alguien abre el archivo. Acá se puede probar
 * con `npm test`, que es `node --test` sobre JavaScript plano.
 *
 * Todas devuelven un arreglo nuevo: el estado de React no se toca en su lugar.
 */

/**
 * La columna que está en `desde` pasa a quedar en la posición `hasta`, corriendo el resto.
 *
 * Sacar y volver a insertar, no intercambiar: arrastrar la columna 1 a la 5 con un intercambio
 * dejaría a la 5 en el lugar de la 1, que no es lo que hizo nadie. Un destino fuera de rango
 * devuelve la lista intacta —pasa al apretar «subir» en la primera—.
 */
export function mover(columnas, desde, hasta) {
  if (hasta < 0 || hasta >= columnas.length || desde < 0 || desde >= columnas.length || desde === hasta) {
    return columnas
  }
  const nuevas = [...columnas]
  const [movida] = nuevas.splice(desde, 1)
  nuevas.splice(hasta, 0, movida)
  return nuevas
}

/** Prende o apaga una columna sin sacarla de su lugar. */
export function alternar(columnas, posicion) {
  return columnas.map((columna, i) => (
    i === posicion ? { ...columna, visible: !columna.visible } : columna
  ))
}

export function prenderTodas(columnas) {
  return columnas.map((columna) => ({ ...columna, visible: true }))
}

/** Las que van a salir en la hoja, en orden. */
export function visibles(columnas) {
  return columnas.filter((columna) => columna.visible)
}

/**
 * Si el borrador difiere de lo guardado. Compara nombre y visibilidad en orden: dos listas con
 * las mismas columnas en distinto orden SÍ son distintas, que es todo el punto de esto.
 */
export function hayCambios(borrador, guardadas) {
  if (borrador.length !== guardadas.length) return true
  return borrador.some((columna, i) => (
    columna.columna !== guardadas[i].columna || columna.visible !== guardadas[i].visible
  ))
}

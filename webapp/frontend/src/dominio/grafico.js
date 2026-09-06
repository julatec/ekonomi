/**
 * Interpreta el JSON de un bloque ```grafico``` de una respuesta del asistente.
 *
 * Separado de `componentes/Grafico.jsx` por lo mismo que separa `marcado.js` de `Marcado.jsx`:
 * esto es lógica pura, comprobable sin levantar React ni un DOM. El componente solo decide qué
 * dibujar con el resultado.
 *
 * Formato esperado: {"titulo": "...", "moneda": "CRC", "series": [{"etiqueta": "...", "valor": 123}]}
 * `titulo` y `moneda` son opcionales. Sin `moneda` los valores se tratan como conteos, no como
 * plata — ver `formatearEntero` vs. `formatearMonto` en el componente.
 *
 * Lanza si el JSON no parsea o no calza con la forma esperada; quien llama decide qué hacer con
 * eso (Grafico.jsx cae al mismo tratamiento que un Mermaid inválido: mostrar el código).
 */
export function analizarGrafico(fuente) {
  const datos = JSON.parse(fuente)
  if (!datos || !Array.isArray(datos.series) || datos.series.length === 0) {
    throw new Error('falta "series"')
  }
  const series = datos.series.map((item, i) => {
    const valor = Number(item?.valor)
    if (!Number.isFinite(valor)) throw new Error(`serie ${i}: "valor" no es un número`)
    return { etiqueta: String(item?.etiqueta ?? ''), valor }
  })
  return {
    titulo: datos.titulo ? String(datos.titulo) : null,
    moneda: datos.moneda ? String(datos.moneda) : null,
    series,
  }
}

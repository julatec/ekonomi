/**
 * Markdown mínimo: del texto que contesta el modelo a una estructura de bloques.
 *
 * Por qué escrito a mano y no `marked` + `dompurify`: el texto que llega acá no es confiable.
 * No por el modelo —que corre en casa— sino por lo que el modelo repite: nombres de emisores,
 * detalles de línea y razones de nota de crédito que salieron de la contabilidad, y adentro de
 * un comprobante firmado puede venir escrito cualquier cosa. Un renderizador que produce HTML
 * obliga a sanear ese HTML después, y el saneador pasa a ser la pieza de la que depende que
 * una factura no ejecute código en la pantalla de quien la lee.
 *
 * Esto nunca construye HTML. Devuelve una estructura de datos, y el componente la convierte en
 * elementos de React, donde una cadena de texto es texto por construcción. La única excepción
 * es el SVG de Mermaid, y está aislada en su propio componente.
 *
 * Se cubre lo que un modelo usa de verdad al contestar sobre facturación: encabezados, listas,
 * tablas, énfasis, código, citas y reglas. Queda afuera lo que no aparece —HTML embebido,
 * notas al pie, enlaces por referencia— y no es un descuido: cada forma que se acepta es una
 * que hay que sostener.
 */

// El bloque de código se reconoce con tres o más cercas para que un ejemplo que contenga ```
// adentro se pueda escribir con cuatro, como manda CommonMark.
const CERCA = /^ {0,3}(`{3,}|~{3,})[ \t]*([^\s`]*)/
const ENCABEZADO = /^ {0,3}(#{1,6})[ \t]+(.*?)[ \t]*#*[ \t]*$/
const REGLA = /^ {0,3}([-*_])[ \t]*(?:\1[ \t]*){2,}$/
const CITA = /^ {0,3}> ?/
const VINETA = /^([ \t]*)([-*+])[ \t]+(.*)$/
const NUMERADA = /^([ \t]*)(\d{1,9})[.)][ \t]+(.*)$/
// La fila separadora es lo que distingue una tabla de un párrafo con barras. Sin ella no hay
// tabla: es la regla de GitHub y también la única forma de no convertir en tabla una línea que
// solo menciona un `|`.
const SEPARADOR = /^ {0,3}\|?[ \t]*:?-+:?[ \t]*(\|[ \t]*:?-+:?[ \t]*)*\|?[ \t]*$/

/** Convierte texto en una lista de bloques. Es la única entrada pública del análisis. */
export function analizar(texto) {
  return bloques(String(texto ?? '').replace(/\r\n?/g, '\n').split('\n'))
}

function bloques(lineas) {
  const salida = []
  let i = 0

  while (i < lineas.length) {
    const linea = lineas[i]

    if (!linea.trim()) { i++; continue }

    const cerca = CERCA.exec(linea)
    if (cerca) {
      const [, marca, lenguaje] = cerca
      const cuerpo = []
      i++
      // Sin cerca de cierre se toma hasta el final. Una respuesta cortada a la mitad —por el
      // tope de tokens del modelo— termina justo así, y perder el código sería peor.
      while (i < lineas.length && !new RegExp(`^ {0,3}${marca[0]}{${marca.length},}[ \t]*$`).test(lineas[i])) {
        cuerpo.push(lineas[i])
        i++
      }
      i++
      salida.push({ tipo: 'codigo', lenguaje: lenguaje.toLowerCase(), texto: cuerpo.join('\n') })
      continue
    }

    const encabezado = ENCABEZADO.exec(linea)
    if (encabezado) {
      salida.push({ tipo: 'encabezado', nivel: encabezado[1].length, partes: enLinea(encabezado[2]) })
      i++
      continue
    }

    if (REGLA.test(linea)) {
      salida.push({ tipo: 'regla' })
      i++
      continue
    }

    if (CITA.test(linea)) {
      const dentro = []
      while (i < lineas.length && (CITA.test(lineas[i]) || lineas[i].trim())) {
        if (!CITA.test(lineas[i]) && !dentro.length) break
        dentro.push(lineas[i].replace(CITA, ''))
        i++
      }
      salida.push({ tipo: 'cita', bloques: bloques(dentro) })
      continue
    }

    if (linea.includes('|') && i + 1 < lineas.length && SEPARADOR.test(lineas[i + 1])) {
      const encabezados = celdas(linea)
      const alineaciones = celdas(lineas[i + 1]).map(alineacion)
      i += 2
      const filas = []
      while (i < lineas.length && lineas[i].includes('|') && lineas[i].trim()) {
        filas.push(celdas(lineas[i]))
        i++
      }
      salida.push({
        tipo: 'tabla',
        alineaciones,
        encabezados: encabezados.map(enLinea),
        // Se empareja cada fila al ancho del encabezado: el modelo a veces manda una celda de
        // menos, y una tabla con filas de largo distinto se desarma sola al pintarla.
        filas: filas.map((fila) => Array.from(
          { length: encabezados.length },
          (_, c) => enLinea(fila[c] ?? ''),
        )),
      })
      continue
    }

    const lista = VINETA.exec(linea) || NUMERADA.exec(linea)
    if (lista) {
      const ordenada = !VINETA.test(linea)
      const items = []
      while (i < lineas.length) {
        const actual = (ordenada ? NUMERADA : VINETA).exec(lineas[i])
        if (!actual) break
        const sangria = ancho(actual[1])
        const dentro = [actual[3]]
        i++
        // Las líneas que siguen pertenecen al ítem si están más sangradas. Así una viñeta
        // anidada, o un párrafo de continuación, entran completos y se analizan recursivamente.
        while (i < lineas.length) {
          const siguiente = lineas[i]
          if (!siguiente.trim()) {
            // Una línea en blanco corta el ítem salvo que lo que siga siga sangrado.
            if (i + 1 < lineas.length && ancho(sangriaDe(lineas[i + 1])) > sangria) {
              dentro.push('')
              i++
              continue
            }
            break
          }
          if (ancho(sangriaDe(siguiente)) <= sangria) break
          dentro.push(siguiente.slice(Math.min(siguiente.length - siguiente.trimStart().length, sangria + 2)))
          i++
        }
        items.push(bloques(dentro))
      }
      salida.push({ tipo: 'lista', ordenada, items })
      continue
    }

    const parrafo = []
    while (i < lineas.length && lineas[i].trim() && !empiezaBloque(lineas[i], lineas[i + 1])) {
      parrafo.push(lineas[i].trim())
      i++
    }
    // Sin esta guarda una línea que ya empieza un bloque —y que llegó acá por el `while` de
    // arriba— dejaría el índice quieto y el ciclo giraría para siempre.
    if (!parrafo.length) {
      parrafo.push(lineas[i].trim())
      i++
    }
    salida.push({ tipo: 'parrafo', partes: enLinea(parrafo.join('\n')) })
  }

  return salida
}

function empiezaBloque(linea, siguiente) {
  return CERCA.test(linea)
    || ENCABEZADO.test(linea)
    || REGLA.test(linea)
    || CITA.test(linea)
    || VINETA.test(linea)
    || NUMERADA.test(linea)
    || (linea.includes('|') && siguiente !== undefined && SEPARADOR.test(siguiente))
}

function sangriaDe(linea) {
  return /^[ \t]*/.exec(linea)[0]
}

/** Una tabulación cuenta como cuatro espacios, que es lo que hace todo lo demás. */
function ancho(sangria) {
  return sangria.replace(/\t/g, '    ').length
}

function celdas(fila) {
  // Se recortan las barras de los extremos —son opcionales— antes de partir, para que
  // `| a | b |` y `a | b` den las mismas dos celdas y no cuatro con dos vacías.
  return fila.trim().replace(/^\|/, '').replace(/\|$/, '').split('|').map((c) => c.trim())
}

function alineacion(celda) {
  if (/^:-+:$/.test(celda)) return 'center'
  if (/-+:$/.test(celda)) return 'right'
  return 'left'
}

// El orden importa: el código va primero porque adentro de un `código` los asteriscos son
// asteriscos. El enlace antes que el énfasis, porque un destino puede traer guiones bajos.
const MARCAS = [
  /(?<cerca>`+)(?<codigo>[\s\S]*?)\k<cerca>/,
  /\[(?<texto>[^\]]*)\]\((?<destino>[^)\s]*)(?:[ \t]+"[^"]*")?\)/,
  /\*\*(?<fuerte>[\s\S]+?)\*\*|__(?<fuerte2>[\s\S]+?)__/,
  /~~(?<tachado>[\s\S]+?)~~/,
  /\*(?<enfasis>[^*\n]+?)\*|(?<![A-Za-z0-9])_(?<enfasis2>[^_\n]+?)_(?![A-Za-z0-9])/,
].map((r) => r.source).join('|')

// Un destino que no sea de estos esquemas se muestra como texto y no como enlace. `javascript:`
// en un href es ejecución, y React no lo impide.
const DESTINO_SEGURO = /^(https?:\/\/|mailto:|\/|#)/i

/** Convierte una línea en partes con énfasis. Exportada porque las celdas la usan directo. */
export function enLinea(texto) {
  const partes = []
  let ultimo = 0
  // Una expresión regular nueva en cada llamada, y no una constante `/g` compartida: `enLinea`
  // se llama a sí misma para el contenido de una negrita, y un `lastIndex` compartido hace que
  // la llamada de adentro le mueva el cursor a la de afuera. El síntoma no es un formato feo,
  // es un ciclo que no termina y se come la memoria de la pestaña. Está en las pruebas.
  const marcas = new RegExp(MARCAS, 'g')

  for (let m = marcas.exec(texto); m; m = marcas.exec(texto)) {
    if (m.index > ultimo) partes.push({ tipo: 'texto', texto: texto.slice(ultimo, m.index) })
    const g = m.groups

    if (g.codigo !== undefined) {
      partes.push({ tipo: 'codigo', texto: g.codigo.trim() })
    } else if (g.destino !== undefined) {
      const contenido = enLinea(g.texto)
      if (DESTINO_SEGURO.test(g.destino)) partes.push({ tipo: 'enlace', destino: g.destino, partes: contenido })
      else partes.push(...contenido)
    } else if (g.fuerte !== undefined || g.fuerte2 !== undefined) {
      partes.push({ tipo: 'fuerte', partes: enLinea(g.fuerte ?? g.fuerte2) })
    } else if (g.tachado !== undefined) {
      partes.push({ tipo: 'tachado', partes: enLinea(g.tachado) })
    } else {
      partes.push({ tipo: 'enfasis', partes: enLinea(g.enfasis ?? g.enfasis2) })
    }
    ultimo = m.index + m[0].length
    // Un calce vacío no avanza el cursor y el ciclo se queda quieto: hay que empujarlo.
    if (m[0].length === 0) marcas.lastIndex++
  }

  if (ultimo < texto.length) partes.push({ tipo: 'texto', texto: texto.slice(ultimo) })
  return partes
}

/** Si hay al menos un diagrama. Sirve para no cargar Mermaid en respuestas que no lo usan. */
export function tieneDiagrama(bloquesAnalizados) {
  return bloquesAnalizados.some((b) => b.tipo === 'codigo' && b.lenguaje === 'mermaid')
}

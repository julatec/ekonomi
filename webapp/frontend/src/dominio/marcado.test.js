/**
 * Pruebas del analizador de Markdown. Se corren con `npm test`, que es `node --test`: el
 * analizador es JavaScript plano sin JSX ni dependencias, así que Node lo ejecuta tal cual y no
 * hace falta traer un corredor de pruebas al build.
 *
 * Lo que se fija acá es lo que el modelo manda de verdad —se sacó de respuestas reales del
 * asistente— y los dos casos que rompen la pantalla si el análisis falla: el ciclo infinito y
 * el enlace con `javascript:`.
 */
import test from 'node:test'
import assert from 'node:assert/strict'
import { analizar, enLinea, tieneDiagrama } from './marcado.js'

test('un parrafo suelto', () => {
  const [b] = analizar('Leda Flores Quesada tuvo 2 facturas.')
  assert.equal(b.tipo, 'parrafo')
  assert.deepEqual(b.partes, [{ tipo: 'texto', texto: 'Leda Flores Quesada tuvo 2 facturas.' }])
})

test('encabezados por nivel', () => {
  assert.deepEqual(
    analizar('# Uno\n\n### Tres').map((b) => [b.tipo, b.nivel]),
    [['encabezado', 1], ['encabezado', 3]],
  )
})

test('negrita, cursiva y codigo en una linea', () => {
  assert.deepEqual(
    enLinea('El total es **₡169.500,00** y el campo es `totalComprobante`, *aproximado*.')
      .map((p) => p.tipo),
    ['texto', 'fuerte', 'texto', 'codigo', 'texto', 'enfasis', 'texto'],
  )
})

test('un guion bajo dentro de una palabra no es cursiva', () => {
  // `resumen_periodo` y `factura_exportacion` aparecen en casi toda respuesta del asistente.
  assert.deepEqual(enLinea('usé resumen_periodo hoy'), [{ tipo: 'texto', texto: 'usé resumen_periodo hoy' }])
})

test('lista con vinetas', () => {
  const [b] = analizar('- CRC: 2 facturas\n- USD: 1 factura')
  assert.equal(b.tipo, 'lista')
  assert.equal(b.ordenada, false)
  assert.equal(b.items.length, 2)
  assert.equal(b.items[1][0].partes[0].texto, 'USD: 1 factura')
})

test('lista numerada y lista anidada', () => {
  const [b] = analizar('1. Ventas\n   - Factura\n   - Tiquete\n2. Compras')
  assert.equal(b.ordenada, true)
  assert.equal(b.items.length, 2)
  const anidada = b.items[0][1]
  assert.equal(anidada.tipo, 'lista')
  assert.equal(anidada.items.length, 2)
})

test('tabla con alineaciones y filas cortas', () => {
  const [b] = analizar('| Moneda | Total |\n|:---|---:|\n| CRC | ₡169.500,00 |\n| USD |')
  assert.equal(b.tipo, 'tabla')
  assert.deepEqual(b.alineaciones, ['left', 'right'])
  assert.equal(b.encabezados.length, 2)
  assert.equal(b.filas.length, 2)
  // La segunda fila trae una celda de menos y se completa: si no, la tabla se desarma.
  assert.equal(b.filas[1].length, 2)
  assert.deepEqual(b.filas[1][1], [])
})

test('una linea con barras no es tabla sin la fila separadora', () => {
  assert.equal(analizar('Emisor | Receptor | Total')[0].tipo, 'parrafo')
})

test('bloque de codigo con lenguaje', () => {
  const [b] = analizar('```sql\nselect 1\n```')
  assert.deepEqual(b, { tipo: 'codigo', lenguaje: 'sql', texto: 'select 1' })
})

test('bloque de codigo sin cerca de cierre llega hasta el final', () => {
  // El modelo se queda sin tokens a mitad de un diagrama más seguido de lo que uno querría.
  const [b] = analizar('```mermaid\ngraph TD;\nA-->B;')
  assert.equal(b.lenguaje, 'mermaid')
  assert.equal(b.texto, 'graph TD;\nA-->B;')
})

test('los asteriscos dentro de codigo no son enfasis', () => {
  assert.deepEqual(enLinea('`a * b * c`'), [{ tipo: 'codigo', texto: 'a * b * c' }])
})

test('cita', () => {
  const [b] = analizar('> Las notas de crédito restan.')
  assert.equal(b.tipo, 'cita')
  assert.equal(b.bloques[0].tipo, 'parrafo')
})

test('regla horizontal', () => {
  assert.equal(analizar('---')[0].tipo, 'regla')
})

test('un enlace con esquema raro queda como texto, no como enlace', () => {
  // Lo que se fija es que NO salga un `enlace`, no el texto exacto: con paréntesis adentro
  // del destino el análisis deja un `)` suelto, y eso es feo pero inofensivo. Lo que no puede
  // pasar es que `javascript:` llegue a un href.
  for (const crudo of ['[tocá acá](javascript:alert(1))', '[ver](javascript:alert%281%29)', '[x](data:text/html,<script>)']) {
    assert.equal(enLinea(crudo).some((p) => p.tipo === 'enlace'), false, crudo)
  }
})

test('un enlace normal si es enlace', () => {
  const [p] = enLinea('[el comprobante](/comprobantes/506...)')
  assert.equal(p.tipo, 'enlace')
  assert.equal(p.destino, '/comprobantes/506...')
})

test('tieneDiagrama distingue mermaid de cualquier otro bloque', () => {
  assert.equal(tieneDiagrama(analizar('```mermaid\ngraph TD;\n```')), true)
  assert.equal(tieneDiagrama(analizar('```js\n1\n```')), false)
})

test('el analisis termina con entradas degeneradas', () => {
  // Un `while` que no avanza cuelga la pestaña entera, no solo el cajón. Vale la prueba.
  for (const entrada of ['', '   ', '\n\n\n', '|', '|||', '- ', '>', '#', '```', '1.', '*', '_ _']) {
    assert.doesNotThrow(() => analizar(entrada), `colgó con ${JSON.stringify(entrada)}`)
  }
})

test('respuesta completa del asistente', () => {
  const real = [
    '### Resumen de setiembre de 2026',
    '',
    'Consulté `resumen_periodo` para la contabilidad **julatec**:',
    '',
    '| Moneda | Tipo | Cantidad | Total |',
    '| --- | --- | ---: | ---: |',
    '| CRC | Factura | 2 | ₡169.500,00 |',
    '| USD | Factura | 1 | $226,00 |',
    '',
    '> Las notas de crédito no se restaron: son ₡22.600,00 aparte.',
  ].join('\n')
  assert.deepEqual(analizar(real).map((b) => b.tipo), ['encabezado', 'parrafo', 'tabla', 'cita'])
})

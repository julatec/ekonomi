/**
 * Pruebas de `analizarGrafico`. Se corren con `npm test` (`node --test`): es JavaScript plano,
 * sin JSX ni dependencias, igual que `marcado.test.js`.
 *
 * Un modelo chico se equivoca de formato con frecuencia —falta "series", "valor" viene como
 * texto, etc.—, y el componente decide qué hacer con eso (mostrar el código en vez de romper).
 * Lo que importa acá es que el análisis distinga con precisión "esto sirve" de "esto no".
 */
import test from 'node:test'
import assert from 'node:assert/strict'
import { analizarGrafico } from './grafico.js'

test('un grafico valido con titulo y moneda', () => {
  const resultado = analizarGrafico(JSON.stringify({
    titulo: 'Facturación por tipo',
    moneda: 'CRC',
    series: [
      { etiqueta: 'Factura', valor: 1250000 },
      { etiqueta: 'Nota de crédito', valor: 85000 },
    ],
  }))
  assert.equal(resultado.titulo, 'Facturación por tipo')
  assert.equal(resultado.moneda, 'CRC')
  assert.deepEqual(resultado.series, [
    { etiqueta: 'Factura', valor: 1250000 },
    { etiqueta: 'Nota de crédito', valor: 85000 },
  ])
})

test('sin titulo ni moneda quedan en null, no en cadena vacia', () => {
  const resultado = analizarGrafico(JSON.stringify({
    series: [{ etiqueta: 'Enero', valor: 3 }],
  }))
  assert.equal(resultado.titulo, null)
  assert.equal(resultado.moneda, null)
})

test('sin "series" falla', () => {
  assert.throws(() => analizarGrafico(JSON.stringify({ titulo: 'Vacío' })))
})

test('"series" vacia falla', () => {
  assert.throws(() => analizarGrafico(JSON.stringify({ series: [] })))
})

test('un valor que no es numero falla', () => {
  assert.throws(() => analizarGrafico(JSON.stringify({
    series: [{ etiqueta: 'Enero', valor: 'mucho' }],
  })))
})

test('JSON invalido falla', () => {
  assert.throws(() => analizarGrafico('esto no es json'))
})

test('una etiqueta faltante se trata como cadena vacia, no revienta', () => {
  const resultado = analizarGrafico(JSON.stringify({ series: [{ valor: 5 }] }))
  assert.equal(resultado.series[0].etiqueta, '')
})

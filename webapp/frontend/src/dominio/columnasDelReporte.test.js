import test from 'node:test'
import assert from 'node:assert/strict'
import { alternar, hayCambios, mover, prenderTodas, visibles } from './columnasDelReporte.js'

const lista = (...nombres) => nombres.map((nombre) => ({ columna: nombre, visible: true }))
const nombres = (columnas) => columnas.map((columna) => columna.columna)

test('mover una columna hacia abajo la deja en el destino y corre las demás', () => {
  assert.deepEqual(nombres(mover(lista('a', 'b', 'c', 'd'), 0, 2)), ['b', 'c', 'a', 'd'])
})

test('mover hacia arriba', () => {
  assert.deepEqual(nombres(mover(lista('a', 'b', 'c', 'd'), 3, 1)), ['a', 'd', 'b', 'c'])
})

test('mover no es intercambiar: a la 5 no manda a la 5 al lugar de la 1', () => {
  // Arrastrar «Fecha» hasta el final tiene que dejar todo lo demás en su orden relativo.
  assert.deepEqual(nombres(mover(lista('Fecha', 'b', 'c', 'd', 'e'), 0, 4)), ['b', 'c', 'd', 'e', 'Fecha'])
})

test('un destino fuera de rango deja la lista intacta', () => {
  const original = lista('a', 'b')
  assert.equal(mover(original, 0, -1), original)
  assert.equal(mover(original, 1, 2), original)
  assert.equal(mover(original, 1, 1), original)
})

test('mover nunca pierde ni duplica una columna', () => {
  const original = lista('a', 'b', 'c', 'd', 'e')
  for (let desde = 0; desde < original.length; desde += 1) {
    for (let hasta = 0; hasta < original.length; hasta += 1) {
      const resultado = nombres(mover(original, desde, hasta))
      assert.equal(resultado.length, original.length)
      assert.deepEqual([...resultado].sort(), ['a', 'b', 'c', 'd', 'e'])
    }
  }
})

test('apagar una columna no la saca de su lugar', () => {
  const resultado = alternar(lista('a', 'b', 'c'), 1)
  assert.deepEqual(nombres(resultado), ['a', 'b', 'c'])
  assert.equal(resultado[1].visible, false)
  assert.deepEqual(nombres(visibles(resultado)), ['a', 'c'])
  assert.equal(alternar(resultado, 1)[1].visible, true, 'volver a prenderla la devuelve a su lugar')
})

test('mostrar todas no cambia el orden', () => {
  const apagadas = alternar(alternar(lista('a', 'b', 'c'), 0), 2)
  assert.deepEqual(nombres(prenderTodas(apagadas)), ['a', 'b', 'c'])
  assert.deepEqual(visibles(prenderTodas(apagadas)).length, 3)
})

test('reordenar cuenta como cambio sin guardar', () => {
  const guardadas = lista('a', 'b', 'c')
  assert.equal(hayCambios(guardadas, guardadas), false)
  assert.equal(hayCambios(mover(guardadas, 0, 1), guardadas), true)
  assert.equal(hayCambios(alternar(guardadas, 0), guardadas), true)
})

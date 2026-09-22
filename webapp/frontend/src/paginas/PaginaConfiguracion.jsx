import React, { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from '../api/client.js'
import { alternar, hayCambios, mover, prenderTodas, visibles } from '../dominio/columnasDelReporte.js'

/**
 * Qué columnas lleva el `.xlsx` de compras y ventas, y en qué orden.
 *
 * Gobierna los dos botones de descarga de «Ventas y compras» —los `.xlsx` que arma el
 * servidor—, no la tabla de comprobantes en pantalla. Quien aplica la preferencia es el
 * backend al escribir el libro: por eso esto se guarda en la base y no en el navegador, y por
 * eso la descarga sigue siendo un `<a href>` corriente, sin nada que el navegador tenga que
 * saber.
 *
 * Las columnas apagadas siguen en la lista y conservan su posición. Es la diferencia entre
 * «apagar» y «botar»: volver a prender una columna la devuelve a donde estaba, no al final.
 */
export default function PaginaConfiguracion() {
  const clienteDeConsultas = useQueryClient()
  const consulta = useQuery({ queryKey: ['columnas-reporte'], queryFn: api.columnasDelReporte })

  // Borrador: reordenar y prender/apagar no viajan hasta que alguien guarda. Un PUT por cada
  // flechita convertiría acomodar treinta y cinco columnas en treinta y cinco escrituras.
  //
  // `null` significa «no he tocado nada» y entonces se muestra lo guardado, en vez de copiarlo
  // a un estado propio. Copiarlo obligaría a sincronizar las dos cosas, y React Query vuelve a
  // consultar al volver a la ventana: sincronizar en cada respuesta le borraría a alguien el
  // acomodo a medio hacer por haberse ido a mirar otra cosa.
  const [borrador, setBorrador] = useState(null)
  const [arrastrando, setArrastrando] = useState(null)
  const [guardado, setGuardado] = useState(false)

  function aplicar(nuevas) {
    setBorrador(nuevas)
    setGuardado(false)
  }

  const guardar = useMutation({
    mutationFn: () => api.guardarColumnasDelReporte(columnas),
    onSuccess: (respuesta) => {
      clienteDeConsultas.setQueryData(['columnas-reporte'], respuesta)
      setBorrador(null)
      setGuardado(true)
    },
  })

  const restaurar = useMutation({
    mutationFn: () => api.restaurarColumnasDelReporte(),
    onSuccess: (respuesta) => {
      clienteDeConsultas.setQueryData(['columnas-reporte'], respuesta)
      setBorrador(null)
      setGuardado(false)
    },
  })

  if (consulta.isPending) return <div className="vacio">Cargando…</div>
  if (consulta.error) {
    return <div className="aviso error">No se pudo leer la configuración: {consulta.error.message}</div>
  }
  const guardadas = consulta.data.columnas
  const columnas = borrador ?? guardadas
  const columnasVisibles = visibles(columnas)
  const sinGuardar = borrador !== null && hayCambios(borrador, guardadas)
  const trabajando = guardar.isPending || restaurar.isPending

  return (
    <>
      <div className="panel">
        <h2>Columnas de los reportes</h2>
        <div className="explicacion">
          <p>
            Esto decide qué lleva el archivo que baja con «↓ sus ventas» y «↓ sus compras», y en
            qué orden salen las columnas. Es tuyo: queda guardado y te sigue a cualquier
            navegador donde entrés con tu certificado.
          </p>
          <p className="tenue pequeno">
            Los nombres son los encabezados que aparecen en la hoja. Si algo más lee ese archivo
            por nombre de columna —una hoja de cálculo con fórmulas, un libro contable—, quitar
            una columna la deja sin ese dato.
          </p>
        </div>

        <div className="barra-acciones">
          <button
            onClick={() => guardar.mutate()}
            disabled={!sinGuardar || trabajando || columnasVisibles.length === 0}
          >
            {guardar.isPending ? 'Guardando…' : 'Guardar'}
          </button>
          <button
            className="chip"
            onClick={() => setBorrador(null)}
            disabled={!sinGuardar || trabajando}
          >
            Descartar los cambios
          </button>
          <button
            className="chip"
            onClick={() => aplicar(prenderTodas(columnas))}
            disabled={trabajando || columnasVisibles.length === columnas.length}
          >
            Mostrar todas
          </button>
          <span className="separador" />
          <button
            className="chip"
            onClick={() => restaurar.mutate()}
            disabled={trabajando || (!consulta.data.personalizada && !sinGuardar)}
            title="Borra tu configuración y vuelve al orden original del reporte"
          >
            {restaurar.isPending ? 'Restaurando…' : 'Volver al orden original'}
          </button>
        </div>

        {sinGuardar && (
          <div className="aviso" style={{ margin: '0 14px 14px' }}>
            Hay cambios sin guardar: el reporte todavía sale como antes.
          </div>
        )}
        {guardado && !sinGuardar && (
          <div className="aviso" style={{ margin: '0 14px 14px' }}>
            Guardado. La próxima descarga ya sale así.
          </div>
        )}
        {guardar.error && (
          <div className="aviso error" style={{ margin: '0 14px 14px' }}>
            {guardar.error.message}
          </div>
        )}
        {restaurar.error && (
          <div className="aviso error" style={{ margin: '0 14px 14px' }}>
            {restaurar.error.message}
          </div>
        )}

        <div className="vista-previa">
          <span className="tenue pequeno">Encabezado del archivo · {columnasVisibles.length} de {columnas.length}</span>
          <div className="mono">
            {columnasVisibles.length === 0
              ? 'Ninguna columna: el reporte tiene que llevar al menos una.'
              : columnasVisibles.map((columna) => columna.columna).join('  |  ')}
          </div>
        </div>

        <ul className="lista-columnas">
          {columnas.map((columna, posicion) => (
            <li
              key={columna.columna}
              className={`${columna.visible ? '' : 'apagada'} ${arrastrando === posicion ? 'arrastrando' : ''}`}
              draggable
              onDragStart={() => setArrastrando(posicion)}
              onDragEnd={() => setArrastrando(null)}
              onDragOver={(evento) => evento.preventDefault()}
              onDrop={(evento) => {
                evento.preventDefault()
                if (arrastrando !== null) aplicar(mover(columnas, arrastrando, posicion))
                setArrastrando(null)
              }}
            >
              {/* El asa no hace nada por sí sola —toda la fila es arrastrable— pero sin algo
                  que lo diga nadie descubre que se puede arrastrar. Las flechas están igual
                  porque arrastrar con el teclado no existe. */}
              <span className="asa" aria-hidden="true">⠿</span>
              <label>
                <input
                  type="checkbox"
                  checked={columna.visible}
                  onChange={() => aplicar(alternar(columnas, posicion))}
                />
                {columna.columna}
              </label>
              <span className="separador" />
              <span className="tenue pequeno mono">{posicion + 1}</span>
              <button
                className="chip"
                onClick={() => aplicar(mover(columnas, posicion, posicion - 1))}
                disabled={posicion === 0}
                aria-label={`Subir ${columna.columna}`}
                title="Subir"
              >
                ↑
              </button>
              <button
                className="chip"
                onClick={() => aplicar(mover(columnas, posicion, posicion + 1))}
                disabled={posicion === columnas.length - 1}
                aria-label={`Bajar ${columna.columna}`}
                title="Bajar"
              >
                ↓
              </button>
            </li>
          ))}
        </ul>
      </div>
    </>
  )
}

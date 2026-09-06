import React, { useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useQuery, keepPreviousData } from '@tanstack/react-query'
import { api } from '../api/client.js'
import { useSesion } from '../estado/SesionContexto.jsx'
import { useDebounce } from '../hooks/useDebounce.js'
import { CAMPOS, comoParametros, interpretarConsulta } from '../dominio/interpretarConsulta.js'
import { ORDEN_TIPOS, tipo as definicionTipo } from '../dominio/tiposDeComprobante.js'
import { conSigno, formatearEntero, formatearMonto } from '../dominio/formato.js'
import EtiquetaTipo from '../componentes/EtiquetaTipo.jsx'
import BandaTotales from '../componentes/BandaTotales.jsx'

const LIMITES = [50, 200, 500]

// Mismo orden y mismas etiquetas que arma el backend en ComprobanteResumen.TARIFAS —el orden
// del Excel (Voucher), no el numérico de los códigos de Hacienda—. Se fija acá para que los
// encabezados salgan siempre, incluso con la tabla vacía, en vez de depender de la primera fila.
const TARIFAS_IVA = [
  '0% Art.32', '0.5%', '1%', '2%', '4%',
  'Transitorio 0%', 'Transitorio 4%', '8%', '13%', 'Exenta', '0% sin crédito',
]

export default function PaginaComprobantes() {
  const { tenant, rango } = useSesion()
  const navegar = useNavigate()
  const [parametrosUrl, setParametrosUrl] = useSearchParams()

  const [texto, setTexto] = useState(parametrosUrl.get('q') || '')
  // `campo` llega en el enlace cuando quien navega ya sabe qué es lo que trae —la lista de
  // contrapartes manda una cédula—, y así no depende de que la adivinanza acierte.
  const [campoForzado, setCampoForzado] = useState(parametrosUrl.get('campo') || null)
  // Filtros por lado del documento. `q` busca en los dos a la vez —que es lo correcto para
  // «todo lo de esta contraparte»— y estos dos responden la pregunta distinta: «lo que ME
  // facturó fulano» contra «lo que YO le facturé». Cada uno acepta cédula o nombre: una cédula
  // no coincide con ningún nombre y un nombre no coincide con ninguna cédula, así que no hace
  // falta decir cuál de las dos se está escribiendo.
  // Venta o compra, RELATIVO A LA CÉDULA BUSCADA y no al dueño de la contabilidad —que la
  // aplicación no conoce—. Es la misma convención que ya usan los reportes:
  // `sales(numero)` busca por emisor y `purchases(numero)` por receptor, o sea desde el punto
  // de vista del número que se pasa. Así el botón «ventas» de una contraparte y este filtro
  // muestran lo mismo, que es lo único que evita dos verdades sobre la misma pantalla.
  const [lado, setLado] = useState(parametrosUrl.get('lado') || 'ambos')

  const [emisor, setEmisor] = useState(parametrosUrl.get('emisor') || '')
  const [receptor, setReceptor] = useState(parametrosUrl.get('receptor') || '')

  // Código de actividad económica: 6 dígitos, existe desde v4.3 de Hacienda. No lleva
  // debounce como los de texto libre —es un código exacto, no hay «coincide parcial» que
  // valga la pena mostrar mientras se escribe— pero sí espera a que tenga los 6 dígitos
  // antes de mandarlo, para no gastar peticiones en un prefijo que el backend va a rechazar.
  const [codigoActividad, setCodigoActividad] = useState(parametrosUrl.get('codigoActividad') || '')

  const [tiposActivos, setTiposActivos] = useState(
    () => new Set((parametrosUrl.get('tipos') || '').split(',').filter(Boolean)),
  )
  const [limite, setLimite] = useState(Number(parametrosUrl.get('limite')) || LIMITES[0])

  // Un enlace como el «ver comprobantes» de una contraparte trae su propio rango para mostrar
  // TODO su histórico, sin importar qué haya puesto la barra superior. Antes esto se adoptaba
  // con cambiarRango(), que escribe la cookie del rango de TODA la aplicación: un solo clic ahí
  // dejaba la barra superior —y cualquier otra pantalla— pegada a «desde 2015» hasta que
  // alguien la cambiara a mano otra vez. Ahora es un override local a esta pantalla nada más:
  // se lee una sola vez al abrir (por eso useState y no useSearchParams directo, que cambiaría
  // en cada edición de los demás filtros) y no toca la cookie ni el estado global.
  const [desdeUrl] = useState(() => parametrosUrl.get('desde'))
  const [hastaUrl] = useState(() => parametrosUrl.get('hasta'))
  const desde = desdeUrl || rango.desde
  const hasta = hastaUrl || rango.hasta
  const rangoForzado = Boolean(desdeUrl || hastaUrl)

  const textoDiferido = useDebounce(texto, 400)
  const emisorDiferido = useDebounce(emisor, 400)
  const receptorDiferido = useDebounce(receptor, 400)
  const codigoActividadDiferido = useDebounce(codigoActividad, 400)
  const interpretacion = useMemo(() => interpretarConsulta(textoDiferido), [textoDiferido])
  const campo = campoForzado || interpretacion.campo

  // El rango de fechas siempre va —es el de la barra superior, el mismo de los reportes— y
  // por sí solo satisface el "hace falta al menos un filtro" del backend, así que la pantalla
  // nunca arranca en un error.
  const parametros = {
    desde,
    hasta,
    limite,
    ...comoParametros(campo, interpretacion.valor, lado),
    ...(emisorDiferido.trim() ? { emisor: emisorDiferido.trim() } : {}),
    ...(receptorDiferido.trim() ? { receptor: receptorDiferido.trim() } : {}),
    ...(codigoActividadDiferido.trim().length === 6 ? { codigoActividad: codigoActividadDiferido.trim() } : {}),
    ...(tiposActivos.size ? { tiposDeComprobante: [...tiposActivos].join(',') } : {}),
  }

  const consulta = useQuery({
    queryKey: ['comprobantes', tenant, parametros],
    queryFn: () => api.comprobantes(parametros),
    placeholderData: keepPreviousData,
  })

  const datos = consulta.data

  function ponerEnUrl(nombre, valor) {
    const url = new URLSearchParams(parametrosUrl)
    if (valor) url.set(nombre, valor)
    else url.delete(nombre)
    setParametrosUrl(url, { replace: true })
  }

  function alternarTipo(clave) {
    setTiposActivos((previos) => {
      const siguiente = new Set(previos)
      if (siguiente.has(clave)) siguiente.delete(clave)
      else siguiente.add(clave)
      const url = new URLSearchParams(parametrosUrl)
      if (siguiente.size) url.set('tipos', [...siguiente].join(','))
      else url.delete('tipos')
      setParametrosUrl(url, { replace: true })
      return siguiente
    })
  }

  return (
    <>
      <div className="buscador">
        <input
          type="search"
          placeholder="Clave, consecutivo, cédula o nombre…"
          value={texto}
          onChange={(evento) => {
            setTexto(evento.target.value)
            setCampoForzado(null)
            const url = new URLSearchParams(parametrosUrl)
            if (evento.target.value) url.set('q', evento.target.value)
            else url.delete('q')
            setParametrosUrl(url, { replace: true })
          }}
        />
        <select value={limite} onChange={(e) => setLimite(Number(e.target.value))}>
          {LIMITES.map((valor) => (
            <option key={valor} value={valor}>{valor} filas</option>
          ))}
        </select>
        {consulta.isFetching && <span className="tenue pequeno">buscando…</span>}
      </div>

      <div className="buscador">
        <input
          type="search"
          placeholder="Emisor: cédula o nombre…"
          value={emisor}
          onChange={(evento) => { setEmisor(evento.target.value); ponerEnUrl('emisor', evento.target.value) }}
        />
        <input
          type="search"
          placeholder="Receptor: cédula o nombre…"
          value={receptor}
          onChange={(evento) => { setReceptor(evento.target.value); ponerEnUrl('receptor', evento.target.value) }}
        />
        <input
          type="search"
          placeholder="Actividad: 6 dígitos…"
          className="mono"
          style={{ maxWidth: 140 }}
          maxLength={6}
          value={codigoActividad}
          onChange={(evento) => {
            const valor = evento.target.value.replace(/\D/g, '').slice(0, 6)
            setCodigoActividad(valor); ponerEnUrl('codigoActividad', valor)
          }}
        />
        {(emisor || receptor || codigoActividad) && (
          <button className="chip" onClick={() => {
            setEmisor(''); setReceptor(''); setCodigoActividad('')
            const url = new URLSearchParams(parametrosUrl)
            url.delete('emisor'); url.delete('receptor'); url.delete('codigoActividad')
            setParametrosUrl(url, { replace: true })
          }}>
            limpiar
          </button>
        )}
      </div>

      {/* Solo aparece con una cédula: sin ella no hay respecto de quién ser venta o compra, y
          un control que no se sabe qué filtra es peor que no tenerlo. */}
      {campo === 'cedula' && interpretacion.valor && (
        <div className="buscador pequeno">
          <span className="tenue">Esa cédula</span>
          {[
            ['ambos', 'en ambos lados'],
            ['venta', 'como emisor · venta'],
            ['compra', 'como receptor · compra'],
          ].map(([valor, etiqueta]) => (
            <button
              key={valor}
              className={`chip ${lado === valor ? 'encendido' : ''}`}
              onClick={() => { setLado(valor); ponerEnUrl('lado', valor === 'ambos' ? '' : valor) }}
            >
              {etiqueta}
            </button>
          ))}
        </div>
      )}

      {/* La interpretación siempre se muestra y siempre se puede corregir: adivinar mal sin
          dejar cambiarlo sería peor que no adivinar. */}
      {campo && interpretacion.valor && (
        <div className="buscador pequeno">
          <span className="tenue">Buscando por</span>
          {Object.keys(CAMPOS).map((nombre) => (
            <button
              key={nombre}
              className={`chip ${campo === nombre ? 'encendido' : ''}`}
              onClick={() => setCampoForzado(nombre)}
            >
              {CAMPOS[nombre]}
            </button>
          ))}
        </div>
      )}

      <div className="buscador">
        {ORDEN_TIPOS.map((clave) => {
          const cuantos = datos?.porTipo?.[clave]
          return (
            <button
              key={clave}
              className={`chip ${tiposActivos.has(clave) ? 'encendido' : ''} ${cuantos === 0 ? 'vacio' : ''}`}
              onClick={() => alternarTipo(clave)}
            >
              {definicionTipo(clave).etiqueta}
              {cuantos !== undefined && ` (${cuantos})`}
            </button>
          )
        })}
      </div>

      {consulta.error && <div className="aviso error">{consulta.error.message}</div>}

      {rangoForzado && (
        <div className="aviso pequeno">
          Mostrando {desde || '(sin piso)'} a {hasta || '(sin techo)'} — el rango de este enlace,
          no el de la barra superior.
        </div>
      )}

      {datos?.truncado && (
        <div className="aviso">
          <span>
            Mostrando {formatearEntero(datos.devueltos)} de {formatearEntero(datos.total)} que
            cumplen el filtro.
          </span>
          {limite < LIMITES[LIMITES.length - 1] && (
            <button onClick={() => setLimite(LIMITES[LIMITES.indexOf(limite) + 1] || 500)}>
              Mostrar más
            </button>
          )}
        </div>
      )}

      <BandaTotales lineas={datos?.lineas} />

      <div className="panel">
        <h2>
          Comprobantes
          {datos ? <span className="tenue pequeno"> · {formatearEntero(datos.total)}</span> : null}
        </h2>
        <div className="desplazable">
          <table>
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Tipo</th>
                <th>Consecutivo</th>
                <th>Emisor</th>
                <th>Receptor</th>
                {TARIFAS_IVA.map((etiqueta) => (
                  <React.Fragment key={etiqueta}>
                    <th style={{ textAlign: 'right' }}>Base {etiqueta}</th>
                    <th style={{ textAlign: 'right' }}>Impuesto {etiqueta}</th>
                  </React.Fragment>
                ))}
                <th style={{ textAlign: 'right' }}>Total</th>
              </tr>
            </thead>
            <tbody>
              {datos?.comprobantes?.map((fila) => {
                const negativo = definicionTipo(fila.tipo).signo < 0
                return (
                  <tr
                    key={fila.clave}
                    className="clicable"
                    onClick={() => navegar(`/comprobantes/${fila.clave}`)}
                  >
                    <td className="mono">{fila.fechaEmision || '—'}</td>
                    <td><EtiquetaTipo valor={fila.tipo} /></td>
                    <td className="mono">{fila.consecutivo}</td>
                    <td>
                      {fila.emisorNombre || '—'}
                      <div className="tenue mono">{fila.emisorNumero}</div>
                      {/* El código solo se muestra filtrando por actividad: fuera de ese
                          caso es un dato que nadie pidió ver y que no cabe cómodo. */}
                      {codigoActividad && fila.codigoActividadEmisor && (
                        <div className="tenue mono pequeno">act. {fila.codigoActividadEmisor}</div>
                      )}
                    </td>
                    <td>
                      {fila.receptorNombre || '—'}
                      <div className="tenue mono">{fila.receptorNumero}</div>
                      {codigoActividad && fila.codigoActividadReceptor && (
                        <div className="tenue mono pequeno">act. {fila.codigoActividadReceptor}</div>
                      )}
                    </td>
                    {(fila.impuestosPorTarifa || []).map((tasa) => (
                      <React.Fragment key={tasa.codigo}>
                        <td className={`monto ${negativo ? 'negativo' : ''}`}>
                          {formatearMonto(conSigno(tasa.baseImponible, fila.tipo), fila.moneda)}
                        </td>
                        <td className={`monto ${negativo ? 'negativo' : ''}`}>
                          {formatearMonto(conSigno(tasa.impuesto, fila.tipo), fila.moneda)}
                        </td>
                      </React.Fragment>
                    ))}
                    <td className={`monto ${negativo ? 'negativo' : ''}`}>
                      {formatearMonto(conSigno(fila.totalComprobante, fila.tipo), fila.moneda)}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
        {datos && datos.comprobantes.length === 0 && (
          <div className="vacio">No hay comprobantes que cumplan ese filtro.</div>
        )}
      </div>
    </>
  )
}

import React from 'react'
import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { api } from '../api/client.js'
import { useSesion } from '../estado/SesionContexto.jsx'
import { conSigno, formatearMonto } from '../dominio/formato.js'
import { esNotaDeAjuste, tipo as definicionTipo } from '../dominio/tiposDeComprobante.js'
import EtiquetaTipo from '../componentes/EtiquetaTipo.jsx'

function Parte({ titulo, parte, nota }) {
  return (
    <div>
      <div className="tenue pequeno">{titulo}</div>
      <div><strong>{parte?.nombre || '—'}</strong></div>
      <div className="tenue mono">{parte?.numero || 'sin identificación'}</div>
      {nota && <div className="tenue pequeno" style={{ marginTop: 4 }}>{nota}</div>}
    </div>
  )
}

/**
 * La factura de compra NO invierte los roles del documento.
 *
 * Se verificó contra el código: `ComprobanteReportService.purchases()` usa `searchByRepecetor`
 * tanto para factura como para factura de compra, sin ramificar por tipo. Y el anexo de
 * Hacienda dice que en la FEC el Emisor es el proveedor. Renombrar las etiquetas por tipo
 * —como se propuso primero— habría contradicho la misma consulta que puso esa fila bajo
 * "compras". Lo que sí es particular se dice como nota, no como intercambio de rótulos.
 */
const EXTRAS = {
  factura_compra: ({ comprobante }) => (
    <p className="tenue pequeno">
      Factura electrónica de compra: la emite y transmite quien recibe el bien o servicio, pero
      el nodo del emisor sigue siendo el proveedor — el mismo orden que en una factura normal.
    </p>
  ),
  factura_exportacion: ({ comprobante }) =>
    comprobante.receptor?.numero ? null : (
      <p className="tenue pequeno">
        El receptor no trae identificación. En exportación es válido: el cliente extranjero
        puede no tener cédula costarricense, y el esquema lo declara opcional.
      </p>
    ),
}

function Referencias({ referencias, claveTipo }) {
  if (!referencias?.length) return null
  return (
    <div className="panel">
      <h2>{esNotaDeAjuste(claveTipo) ? 'Documento que ajusta' : 'Referencias'}</h2>
      <div className="desplazable">
        <table>
          <thead>
            <tr>
              <th>Tipo</th><th>Número</th><th>Fecha</th><th>Motivo</th><th>Razón</th>
            </tr>
          </thead>
          <tbody>
            {referencias.map((referencia, indice) => (
              <tr key={`${referencia.numero}-${indice}`}>
                {/* Las Notas 9 y 10 de los anexos piden mostrar la descripción del código,
                    no el código. Va igual debajo, en pequeño: es lo que trae el documento
                    firmado y lo que hay que poder citar. */}
                <td>
                  {referencia.descripcionTipoDoc || '—'}
                  {referencia.tipoDoc && referencia.tipoDoc !== referencia.descripcionTipoDoc && (
                    <div className="tenue mono pequeno">{referencia.tipoDoc}</div>
                  )}
                </td>
                <td className="mono">{referencia.numero || '—'}</td>
                <td className="mono">{referencia.fechaEmision || '—'}</td>
                <td>
                  {referencia.descripcionCodigo || '—'}
                  {referencia.codigo && referencia.codigo !== referencia.descripcionCodigo && (
                    <div className="tenue mono pequeno">{referencia.codigo}</div>
                  )}
                </td>
                <td>{referencia.razon || '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function Impuestos({ impuestos, moneda }) {
  if (!impuestos?.length) return <span className="tenue">—</span>
  return (
    <>
      {impuestos.map((impuesto, indice) => (
        <span
          key={indice}
          className="chip"
          style={{ marginRight: 4 }}
          title={`${impuesto.descripcionTarifa || 'tarifa sin código'} · ${formatearMonto(impuesto.monto, moneda)}`}
        >
          {impuesto.tarifaPorcentaje !== null && impuesto.tarifaPorcentaje !== undefined
            ? `${impuesto.tarifaPorcentaje}%`
            : '—'}
          {impuesto.exoneracion && ' · exonerado'}
        </span>
      ))}
    </>
  )
}

export default function PaginaComprobante() {
  const { clave } = useParams()
  const { tenant } = useSesion()

  const consulta = useQuery({
    queryKey: ['comprobante', tenant, clave],
    queryFn: () => api.comprobante(clave),
  })

  if (consulta.error) {
    return (
      <>
        <p><Link to="/comprobantes">← Volver a la búsqueda</Link></p>
        <div className="aviso error">{consulta.error.message}</div>
      </>
    )
  }
  // `isPending` y no `isLoading`: mientras React Query espera entre reintentos el
  // `fetchStatus` queda en `paused`, y ahí `isLoading` es falso con `data` todavía en
  // `undefined` y `error` todavía en `null`. Ese hueco basta para tumbar la pantalla.
  if (consulta.isPending) return <div className="vacio">Cargando el comprobante…</div>

  const comprobante = consulta.data
  const moneda = comprobante.resumen?.moneda
  const Extra = EXTRAS[comprobante.tipo]
  const resta = definicionTipo(comprobante.tipo).signo < 0
  const ajusta = esNotaDeAjuste(comprobante.tipo)

  return (
    <>
      <p><Link to="/comprobantes">← Volver a la búsqueda</Link></p>

      {/* Nota 1 de los anexos de Hacienda: el tipo de documento, la clave y el consecutivo
          van juntos en la representación gráfica. */}
      <div className="panel">
        <h2>
          <EtiquetaTipo valor={comprobante.tipo} />{' '}
          <span className="mono">{comprobante.consecutivo}</span>{' '}
          <span className="tenue pequeno">· {comprobante.fechaEmision}</span>
        </h2>
        <div style={{ padding: 14, display: 'grid', gap: 16, gridTemplateColumns: '1fr 1fr' }}>
          <Parte titulo="Emisor" parte={comprobante.emisor} />
          <Parte titulo="Receptor" parte={comprobante.receptor} />
          <div style={{ gridColumn: '1 / -1' }}>
            <div className="tenue pequeno">Clave</div>
            <div className="mono">{comprobante.clave}</div>
          </div>
          {Extra && (
            <div style={{ gridColumn: '1 / -1' }}>
              <Extra comprobante={comprobante} />
            </div>
          )}
        </div>
      </div>

      {/* En una nota, la referencia va antes de los totales: sin saber qué documento ajusta,
          los montos no significan nada. Vale para las dos, no solo para la de crédito: la de
          débito suma, pero tampoco se entiende sola. */}
      {ajusta && <Referencias referencias={comprobante.referencias} claveTipo={comprobante.tipo} />}

      <div className="panel">
        <h2>Líneas de detalle</h2>
        <div className="desplazable">
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Detalle</th>
                <th style={{ textAlign: 'right' }}>Cantidad</th>
                <th style={{ textAlign: 'right' }}>Precio</th>
                <th style={{ textAlign: 'right' }}>Subtotal</th>
                <th>Impuestos</th>
                <th style={{ textAlign: 'right' }}>Total línea</th>
              </tr>
            </thead>
            <tbody>
              {comprobante.lineas.map((linea) => (
                <tr key={linea.numeroLinea}>
                  <td className="mono">{linea.numeroLinea}</td>
                  <td>{linea.detalle}</td>
                  <td className="monto">{linea.cantidad}</td>
                  <td className="monto">{formatearMonto(linea.precioUnitario, moneda)}</td>
                  <td className="monto">{formatearMonto(linea.subTotal, moneda)}</td>
                  <td><Impuestos impuestos={linea.impuestos} moneda={moneda} /></td>
                  <td className="monto">{formatearMonto(linea.montoTotalLinea, moneda)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {comprobante.lineas.length === 0 && (
          <div className="vacio">El documento no trae líneas de detalle.</div>
        )}
      </div>

      {comprobante.resumen && (
        <div className="panel">
          <h2>Totales</h2>
          <div className="desplazable">
            <table>
              <tbody>
                {[
                  ['Gravado', comprobante.resumen.totalGravado],
                  ['Exento', comprobante.resumen.totalExento],
                  ['Venta neta', comprobante.resumen.totalVentaNeta],
                  ['Descuentos', comprobante.resumen.totalDescuentos],
                  ['Otros cargos', comprobante.resumen.totalOtrosCargos],
                  ['Impuesto', comprobante.resumen.totalImpuesto],
                ].map(([titulo, valor]) => (
                  <tr key={titulo}>
                    <td className="tenue">{titulo}</td>
                    <td className="monto">{formatearMonto(valor, moneda)}</td>
                  </tr>
                ))}
                <tr>
                  <td><strong>Total del comprobante</strong></td>
                  <td className={`monto ${resta ? 'negativo' : ''}`}>
                    <strong>
                      {formatearMonto(
                        conSigno(comprobante.resumen.totalComprobante, comprobante.tipo),
                        moneda,
                      )}
                    </strong>
                    {resta && <div className="tenue pequeno">resta del período</div>}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}

      {!ajusta && <Referencias referencias={comprobante.referencias} claveTipo={comprobante.tipo} />}
    </>
  )
}

import React from 'react'
import { useIsFetching } from '@tanstack/react-query'
import { useSesion } from '../estado/SesionContexto.jsx'

/**
 * Los únicos dos tenants con franja/insignia propia hoy. Uno nuevo que aparezca en
 * `tenants` sin estar acá simplemente no tiene señal visual —ver el comentario de
 * `.superior.tenant-julatec` en estilos.css—, no revienta nada.
 */
const TITULO_TENANT = {
  julatec: 'Contabilidad personal (julatec)',
  tribuconta: 'Contabilidad de Tribuconta',
}

/**
 * El rango que vive acá es el de toda la aplicación: filtra la búsqueda de comprobantes y es
 * el que se lleva puesto el reporte `.xlsx` que se baja desde la lista de contrapartes. Por
 * eso está en la barra y no dentro de una pantalla.
 */
export default function BarraSuperior() {
  const { sesion, tenants, tenant, rango, cambiarTenant, cambiarRango } = useSesion()
  const tituloTenant = TITULO_TENANT[tenant]
  // Cambiar el rango (o el tenant) dispara refetches en la pantalla que esté activa, pero no
  // todas muestran su propio "buscando…" —el detalle de un comprobante, o un buscador vacío,
  // no tienen ninguna query visible—. `useIsFetching` cuenta TODAS las queries en curso en
  // toda la app, así que esto avisa sin importar qué pantalla esté abierta ni si tiene su
  // propio indicador local.
  const actualizando = useIsFetching() > 0

  return (
    <header className={`superior ${tituloTenant ? `tenant-${tenant}` : ''}`}>
      <div>
        <label htmlFor="tenant">Contabilidad</label>
        <select
          id="tenant"
          value={tenant || ''}
          onChange={(evento) => cambiarTenant(evento.target.value)}
        >
          {tenants.map((nombre) => (
            <option key={nombre} value={nombre}>{nombre}</option>
          ))}
        </select>
        {/* Un punto y no una palabra: la franja de arriba ya dice qué contabilidad es esta,
            esto solo lo confirma junto al control mismo con el que se cambia. */}
        {tituloTenant && (
          <span className={`insignia-tenant tenant-${tenant}`} title={tituloTenant} />
        )}
      </div>

      <div>
        <label htmlFor="desde">Desde</label>
        <input
          id="desde"
          type="date"
          value={rango.desde}
          onChange={(evento) => cambiarRango(evento.target.value, null)}
        />
      </div>

      <div>
        <label htmlFor="hasta">Hasta</label>
        <input
          id="hasta"
          type="date"
          value={rango.hasta}
          onChange={(evento) => cambiarRango(null, evento.target.value)}
        />
      </div>

      <span className="separador" />
      {actualizando && <span className="tenue pequeno">actualizando…</span>}
      <span className="tenue pequeno">{sesion?.username}</span>
    </header>
  )
}

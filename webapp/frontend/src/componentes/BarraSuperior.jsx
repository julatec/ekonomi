import React from 'react'
import { useSesion } from '../estado/SesionContexto.jsx'

/**
 * El rango que vive acá es el de toda la aplicación: filtra la búsqueda de comprobantes y es
 * el que se lleva puesto el reporte `.xlsx` que se baja desde la lista de contrapartes. Por
 * eso está en la barra y no dentro de una pantalla.
 */
export default function BarraSuperior() {
  const { sesion, tenants, tenant, rango, cambiarTenant, cambiarRango } = useSesion()

  return (
    <header className="superior">
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
      <span className="tenue pequeno">{sesion?.username}</span>
    </header>
  )
}

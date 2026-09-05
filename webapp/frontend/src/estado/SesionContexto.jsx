import React, { createContext, useContext, useEffect, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { api, ponerCookie } from '../api/client.js'

const Contexto = createContext(null)

/** El backend serializa las fechas como instantes ISO; los `input type=date` quieren AAAA-MM-DD. */
function comoFecha(valor) {
  return valor ? String(valor).slice(0, 10) : ''
}

/**
 * El tenant y el rango de fechas siguen viviendo en las mismas cookies que ya lee el backend
 * (`Workspace.setRequest`): no se inventa un mecanismo nuevo.
 *
 * Lo que sí cambia es que cambiar de tenant ya no recarga la página entera. Antes era
 * `window.location.reload()`; ahora se escribe la cookie y se invalidan las consultas, porque
 * el backend resuelve el tenant en cada petición y no solo al construir el Workspace.
 *
 * El rango es uno solo para toda la aplicación, y esa es una decisión, no una simplificación:
 * las mismas cookies gobiernan los reportes `.xlsx` que se bajan desde la lista de
 * contrapartes. Con un rango propio en la pantalla de búsqueda, alguien podía buscar en cinco
 * años, apretar «compras» y bajarse el reporte de tres meses sin que nada se lo dijera.
 */
export function ProveedorSesion({ children }) {
  const queryClient = useQueryClient()
  const consulta = useQuery({ queryKey: ['sesion'], queryFn: api.sesion })

  // Copia local del rango: la cookie no se puede leer de vuelta ya normalizada sin volver a
  // pedir la sesión, y los campos de fecha tienen que reflejar lo que el usuario acaba de
  // escribir en el mismo instante.
  const [rango, setRango] = useState(null)
  useEffect(() => {
    if (consulta.data && rango === null) {
      setRango({ desde: comoFecha(consulta.data.desde), hasta: comoFecha(consulta.data.hasta) })
    }
  }, [consulta.data, rango])

  const valor = {
    sesion: consulta.data,
    // `isPending` y no `isLoading`: entre reintentos React Query pasa el `fetchStatus` a
    // `paused`, y ahí `isLoading` es falso aunque todavía no haya ni datos ni error. Con
    // `isLoading` la pantalla se renderiza con la sesión en `undefined` y revienta.
    // También mientras el rango no esté copiado: si la pantalla se dibuja con el rango en
    // blanco, la búsqueda sale sin ningún filtro y el backend la rechaza con un 400.
    cargando: consulta.isPending || rango === null,
    error: consulta.error,
    tenant: consulta.data?.tenant,
    tenants: consulta.data?.tenants || [],
    mensajes: consulta.data?.mensajes || {},
    rango: rango || { desde: '', hasta: '' },

    cambiarTenant: (tenant) => {
      ponerCookie('tenant', tenant)
      // Todo lo consultado pertenece al tenant anterior. Se limpia entero en vez de
      // refrescar por partes: dejar filas de una contabilidad bajo el rótulo de otra, aunque
      // sea por un instante, es el peor fallo posible de esta pantalla.
      queryClient.clear()
      consulta.refetch()
    },

    cambiarRango: (desde, hasta) => {
      if (desde) ponerCookie('rangeLower', desde)
      if (hasta) ponerCookie('rangeUpper', hasta)
      setRango((previo) => ({
        desde: desde || previo?.desde || '',
        hasta: hasta || previo?.hasta || '',
      }))
      queryClient.invalidateQueries()
    },
  }

  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>
}

export function useSesion() {
  const valor = useContext(Contexto)
  if (!valor) throw new Error('useSesion fuera del ProveedorSesion')
  return valor
}

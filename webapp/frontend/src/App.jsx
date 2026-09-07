import React, { useEffect, useState } from 'react'
import { NavLink, Navigate, Route, Routes } from 'react-router-dom'
import { SESION_VENCIDA } from './api/client.js'
import { ProveedorSesion, useSesion } from './estado/SesionContexto.jsx'
import BarraSuperior from './componentes/BarraSuperior.jsx'
import CajonAsistente from './componentes/CajonAsistente.jsx'
import PaginaComprobantes from './paginas/PaginaComprobantes.jsx'
import PaginaComprobante from './paginas/PaginaComprobante.jsx'
import PaginaClientes from './paginas/PaginaClientes.jsx'
import PaginaCabys from './paginas/PaginaCabys.jsx'
import PaginaActividades from './paginas/PaginaActividades.jsx'

/**
 * Un `fetch` no puede hacer que el navegador vuelva a presentar el certificado: eso solo pasa
 * en un handshake nuevo, o sea en una navegación. Por eso ante un 401 no se ofrece reintentar
 * —no hay nada que reintentar— sino recargar, que es la única salida.
 */
function SesionVencida() {
  return (
    <div className="bloqueo">
      <h2>La sesión ya no es válida</h2>
      <p>El certificado de firma digital dejó de ser aceptado para esta sesión.</p>
      <button onClick={() => window.location.reload()}>Recargar la página</button>
    </div>
  )
}

function Lateral() {
  return (
    <aside className="lateral">
      <h1>Ekonomi</h1>
      <nav>
        {/* Primera opción a propósito: es donde se bajan los reportes .xlsx de ventas y
            compras, lo que más se usa día a día — "Contrapartes" describía la tabla que
            arma la pantalla, no para qué la abre alguien. */}
        <NavLink to="/clientes" className={({ isActive }) => (isActive ? 'activo' : '')}>
          Ventas y compras
        </NavLink>
        <NavLink to="/comprobantes" className={({ isActive }) => (isActive ? 'activo' : '')}>
          Comprobantes
        </NavLink>
        <NavLink to="/cabys" className={({ isActive }) => (isActive ? 'activo' : '')}>
          Catálogo CABYS
        </NavLink>
        <NavLink to="/actividades" className={({ isActive }) => (isActive ? 'activo' : '')}>
          Actividades Hacienda
        </NavLink>
      </nav>
    </aside>
  )
}

function Contenido() {
  const { cargando, error } = useSesion()
  const [asistenteAbierto, setAsistenteAbierto] = useState(false)

  if (cargando) return <div className="vacio">Cargando…</div>
  if (error) {
    return (
      <div className="contenido">
        <div className="aviso error">No se pudo cargar la sesión: {error.message}</div>
      </div>
    )
  }

  return (
    <>
      <Lateral />
      <div className="principal">
        <BarraSuperior />
        <main className="contenido">
          <Routes>
            <Route path="/" element={<Navigate to="/comprobantes" replace />} />
            <Route path="/comprobantes" element={<PaginaComprobantes />} />
            <Route path="/comprobantes/:clave" element={<PaginaComprobante />} />
            <Route path="/clientes" element={<PaginaClientes />} />
            <Route path="/cabys" element={<PaginaCabys />} />
            <Route path="/actividades" element={<PaginaActividades />} />
            <Route path="*" element={<div className="vacio">Esa página no existe.</div>} />
          </Routes>
        </main>
      </div>

      {/* El hueco del asistente: un botón flotante que abre un cajón lateral, para que el
          contenido principal conserve todo el ancho cuando no se está usando. */}
      {!asistenteAbierto && (
        <button className="boton-chat" onClick={() => setAsistenteAbierto(true)}>
          Asistente
        </button>
      )}
      {asistenteAbierto && <CajonAsistente onCerrar={() => setAsistenteAbierto(false)} />}
    </>
  )
}

export default function App() {
  const [vencida, setVencida] = useState(false)

  useEffect(() => {
    const alVencer = () => setVencida(true)
    window.addEventListener(SESION_VENCIDA, alVencer)
    return () => window.removeEventListener(SESION_VENCIDA, alVencer)
  }, [])

  return (
    <div className="aplicacion">
      <ProveedorSesion>
        <Contenido />
      </ProveedorSesion>
      {vencida && <SesionVencida />}
    </div>
  )
}

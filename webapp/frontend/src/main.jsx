import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from './App.jsx'
import './estilos.css'

// Sin reintentos automáticos ante un 401: el certificado no se puede volver a presentar desde
// un fetch, así que reintentar solo repetiría el fallo tres veces antes de mostrarlo.
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (intentos, error) => error?.status !== 401 && intentos < 2,
      refetchOnWindowFocus: false,
      staleTime: 30_000,
    },
  },
})

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </QueryClientProvider>
  </React.StrictMode>,
)

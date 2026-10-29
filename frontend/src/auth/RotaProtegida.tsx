import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useSessao } from './useSessao'

/** Envia para o login quem não tem sessão, lembrando a página pedida. */
export function RotaProtegida({ children }: { children: ReactNode }) {
  const { utilizador } = useSessao()
  const local = useLocation()
  if (!utilizador) return <Navigate to="/login" replace state={{ de: local.pathname }} />
  return children
}

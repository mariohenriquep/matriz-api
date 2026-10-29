import { createContext } from 'react'
import type { Utilizador } from '../api/autenticacao'

export interface EstadoSessao {
  utilizador: Utilizador | null
  /** Mensagem a mostrar no ecrã de login quando a sessão terminou sem o utilizador pedir. */
  aviso: string | null
  entrar: (email: string, password: string) => Promise<void>
  sair: () => Promise<void>
}

export const ContextoSessao = createContext<EstadoSessao | null>(null)

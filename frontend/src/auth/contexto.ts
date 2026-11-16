import { createContext } from 'react'
import type { Conta } from '../api/autenticacao'

export interface EstadoSessao {
  utilizador: Conta | null
  /** Mensagem a mostrar no ecrã de login quando a sessão terminou sem o utilizador pedir. */
  aviso: string | null
  entrar: (email: string, password: string) => Promise<void>
  sair: () => Promise<void>
}

export const ContextoSessao = createContext<EstadoSessao | null>(null)

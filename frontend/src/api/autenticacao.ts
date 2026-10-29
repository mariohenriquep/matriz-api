import { pedido } from './cliente'

export interface Utilizador {
  id: string
  nome: string
  email: string
}

export interface Sessao {
  token: string
  expiraEm: string
  utilizador: Utilizador
}

export function iniciarSessao(email: string, password: string) {
  return pedido<Sessao>('/auth/login', { metodo: 'POST', corpo: { email, password }, autenticado: false })
}

export function obterUtilizadorAutenticado() {
  return pedido<Utilizador>('/auth/eu')
}

export function terminarSessao() {
  return pedido<void>('/auth/logout', { metodo: 'POST' })
}

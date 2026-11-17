import { pedido } from './cliente'

export interface Utilizador {
  id: string
  nome: string
  email: string
}

/** Plano da conta: o gratuito (registo público) só participa nos projetos para que é convidado. */
export type Plano = 'GRATUITO' | 'PROFISSIONAL'

/** O utilizador autenticado, com o plano (só o próprio o vê). */
export interface Conta extends Utilizador {
  plano: Plano
  podeCriarProjetos: boolean
}

export interface Sessao {
  token: string
  expiraEm: string
  utilizador: Conta
}

export interface PedidoRegisto {
  nome: string
  email: string
  password: string
  /** Campo escondido das pessoas: só um robô o preenche (o backend ignora o registo). */
  site: string
}

export function iniciarSessao(email: string, password: string) {
  return pedido<Sessao>('/auth/login', { metodo: 'POST', corpo: { email, password }, autenticado: false })
}

/** A resposta é sempre a mesma mensagem, exista ou não uma conta com o email (para não o revelar). */
export function registar(dados: PedidoRegisto) {
  return pedido<{ mensagem: string }>('/auth/registo', { metodo: 'POST', corpo: dados, autenticado: false })
}

export function obterUtilizadorAutenticado() {
  return pedido<Conta>('/auth/eu')
}

export function terminarSessao() {
  return pedido<void>('/auth/logout', { metodo: 'POST' })
}

// Sessão guardada no sessionStorage (decisão do Mário, Ficha 03 Ex. 3): sobrevive a recarregar a página
// e desaparece quando o separador fecha. O token expira 8 horas depois do login.
import type { Sessao } from '../api/autenticacao'

const CHAVE = 'matriz.sessao'
export const EVENTO_SESSAO_TERMINADA = 'matriz:sessao-terminada'

export function lerSessao(): Sessao | null {
  try {
    const texto = sessionStorage.getItem(CHAVE)
    if (!texto) return null
    const sessao = JSON.parse(texto) as Sessao
    if (new Date(sessao.expiraEm).getTime() <= Date.now()) {
      sessionStorage.removeItem(CHAVE)
      return null
    }
    return sessao
  } catch {
    return null
  }
}

export function guardarSessao(sessao: Sessao) {
  try {
    sessionStorage.setItem(CHAVE, JSON.stringify(sessao))
  } catch {
    // Sem armazenamento (ex.: modo privado restrito): a sessão dura só enquanto a página estiver aberta.
  }
}

export function limparSessao() {
  try {
    sessionStorage.removeItem(CHAVE)
  } catch {
    // ignorar
  }
}

/** Chamado pelo cliente da API quando o backend responde 401 a um pedido autenticado. */
export function avisarSessaoTerminada() {
  limparSessao()
  window.dispatchEvent(new Event(EVENTO_SESSAO_TERMINADA))
}

import { pedido } from './cliente'
import type { Utilizador } from './autenticacao'

export type Papel = 'GESTOR' | 'MEMBRO'

export interface Contagens {
  POR_FAZER: number
  EM_CURSO: number
  CONCLUIDA: number
  total: number
}

export interface Projeto {
  id: string
  nome: string
  descricao: string | null
  gestor: { id: string; nome: string }
  meuPapel: Papel
  contagens: Contagens
  criadoEm: string
  atualizadoEm: string
}

export interface Membro {
  utilizador: Utilizador
  papel: Papel
}

export interface Lista<T> {
  itens: T[]
  total: number
}

export const LIMITE_NOME = 100
export const LIMITE_DESCRICAO = 1000

export function listarProjetos(pesquisa?: string) {
  const consulta = pesquisa?.trim() ? `?pesquisa=${encodeURIComponent(pesquisa.trim())}` : ''
  return pedido<Lista<Projeto>>(`/projetos${consulta}`)
}

export function obterProjeto(id: string) {
  return pedido<Projeto>(`/projetos/${encodeURIComponent(id)}`)
}

export function criarProjeto(dados: { nome: string; descricao: string | null }) {
  return pedido<Projeto>('/projetos', { metodo: 'POST', corpo: dados })
}

export function editarProjeto(id: string, dados: { nome?: string; descricao?: string | null }) {
  return pedido<Projeto>(`/projetos/${encodeURIComponent(id)}`, { metodo: 'PATCH', corpo: dados })
}

export function eliminarProjeto(id: string) {
  return pedido<void>(`/projetos/${encodeURIComponent(id)}`, { metodo: 'DELETE' })
}

export function listarMembros(projetoId: string) {
  return pedido<Lista<Membro>>(`/projetos/${encodeURIComponent(projetoId)}/membros`)
}

export function adicionarMembro(projetoId: string, email: string) {
  return pedido<Membro>(`/projetos/${encodeURIComponent(projetoId)}/membros`, { metodo: 'POST', corpo: { email } })
}

export function removerMembro(projetoId: string, utilizadorId: string) {
  return pedido<void>(`/projetos/${encodeURIComponent(projetoId)}/membros/${encodeURIComponent(utilizadorId)}`, {
    metodo: 'DELETE',
  })
}

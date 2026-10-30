import { pedido } from './cliente'
import type { Lista } from './projetos'

export type EstadoTarefa = 'POR_FAZER' | 'EM_CURSO' | 'CONCLUIDA'
export type Prioridade = 'BAIXA' | 'MEDIA' | 'ALTA'

export const ESTADOS: { valor: EstadoTarefa; nome: string }[] = [
  { valor: 'POR_FAZER', nome: 'Por fazer' },
  { valor: 'EM_CURSO', nome: 'Em curso' },
  { valor: 'CONCLUIDA', nome: 'Concluída' },
]

export const PRIORIDADES: { valor: Prioridade; nome: string }[] = [
  { valor: 'ALTA', nome: 'Alta' },
  { valor: 'MEDIA', nome: 'Média' },
  { valor: 'BAIXA', nome: 'Baixa' },
]

export const LIMITE_TITULO = 200
export const LIMITE_DESCRICAO_TAREFA = 2000

export interface Pessoa {
  id: string
  nome: string
}

export interface Tarefa {
  id: string
  projetoId: string
  titulo: string
  descricao: string | null
  estado: EstadoTarefa
  prioridade: Prioridade
  responsaveis: Pessoa[]
  criador: Pessoa
  dataLimite: string | null
  atrasada: boolean
  totalComentarios: number
  criadoEm: string
  atualizadoEm: string
}

export interface FiltrosTarefas {
  estado?: EstadoTarefa
  prioridade?: Prioridade
  responsavelId?: string
}

export interface NovaTarefa {
  titulo: string
  descricao: string | null
  prioridade: Prioridade
  dataLimite: string | null
  responsavelIds: string[]
}

export type AlteracaoTarefa = Partial<Omit<NovaTarefa, 'responsavelIds'> & { estado: EstadoTarefa }>

export function listarTarefas(projetoId: string, filtros: FiltrosTarefas = {}) {
  const consulta = new URLSearchParams()
  if (filtros.estado) consulta.set('estado', filtros.estado)
  if (filtros.prioridade) consulta.set('prioridade', filtros.prioridade)
  if (filtros.responsavelId) consulta.set('responsavelId', filtros.responsavelId)
  const texto = consulta.toString()
  return pedido<Lista<Tarefa>>(`/projetos/${encodeURIComponent(projetoId)}/tarefas${texto ? `?${texto}` : ''}`)
}

export function criarTarefa(projetoId: string, dados: NovaTarefa) {
  return pedido<Tarefa>(`/projetos/${encodeURIComponent(projetoId)}/tarefas`, { metodo: 'POST', corpo: dados })
}

export function editarTarefa(id: string, dados: AlteracaoTarefa) {
  return pedido<Tarefa>(`/tarefas/${encodeURIComponent(id)}`, { metodo: 'PATCH', corpo: dados })
}

export function eliminarTarefa(id: string) {
  return pedido<void>(`/tarefas/${encodeURIComponent(id)}`, { metodo: 'DELETE' })
}

export function nomeEstado(estado: EstadoTarefa) {
  return ESTADOS.find((e) => e.valor === estado)?.nome ?? estado
}

export function nomePrioridade(prioridade: Prioridade) {
  return PRIORIDADES.find((p) => p.valor === prioridade)?.nome ?? prioridade
}

/** "2026-11-15" → "15/11/2026", sem passar por fusos horários. */
export function formatarData(data: string) {
  const [ano, mes, dia] = data.split('-')
  return `${dia}/${mes}/${ano}`
}

export interface Comentario {
  id: string
  conteudo: string
  autor: Pessoa
  criadoEm: string
}

export const LIMITE_COMENTARIO = 2000

export function obterTarefa(id: string) {
  return pedido<Tarefa>(`/tarefas/${encodeURIComponent(id)}`)
}

export function atribuirResponsavel(tarefaId: string, utilizadorId: string) {
  return pedido<Tarefa>(`/tarefas/${encodeURIComponent(tarefaId)}/responsaveis`, {
    metodo: 'POST',
    corpo: { utilizadorId },
  })
}

export function removerResponsavel(tarefaId: string, utilizadorId: string) {
  return pedido<void>(
    `/tarefas/${encodeURIComponent(tarefaId)}/responsaveis/${encodeURIComponent(utilizadorId)}`,
    { metodo: 'DELETE' },
  )
}

export function listarComentarios(tarefaId: string) {
  return pedido<Lista<Comentario>>(`/tarefas/${encodeURIComponent(tarefaId)}/comentarios`)
}

export function comentar(tarefaId: string, conteudo: string) {
  return pedido<Comentario>(`/tarefas/${encodeURIComponent(tarefaId)}/comentarios`, {
    metodo: 'POST',
    corpo: { conteudo },
  })
}

/** Data e hora locais do browser, ex.: "29/10/2026, 21:30". */
export function formatarDataHora(instante: string) {
  return new Date(instante).toLocaleString('pt-PT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// Dados fictícios partilhados pelos testes.
import type { Utilizador } from '../api/autenticacao'
import type { Membro, Projeto } from '../api/projetos'
import type { Tarefa } from '../api/tarefas'

export const ana: Utilizador = { id: 'u-ana', nome: 'Ana Gestora', email: 'ana@exemplo.pt' }
export const rui: Utilizador = { id: 'u-rui', nome: 'Rui Membro', email: 'rui@exemplo.pt' }

export const membros: Membro[] = [
  { utilizador: ana, papel: 'GESTOR' },
  { utilizador: rui, papel: 'MEMBRO' },
]

export function umProjeto(alteracoes: Partial<Projeto> = {}): Projeto {
  return {
    id: 'p-1',
    nome: 'Website',
    descricao: null,
    gestor: { id: ana.id, nome: ana.nome },
    meuPapel: 'GESTOR',
    contagens: { POR_FAZER: 1, EM_CURSO: 1, CONCLUIDA: 1, total: 3 },
    criadoEm: '2026-10-01T09:00:00Z',
    atualizadoEm: '2026-10-01T09:00:00Z',
    ...alteracoes,
  }
}

export function umaTarefa(alteracoes: Partial<Tarefa> = {}): Tarefa {
  return {
    id: 't-1',
    projetoId: 'p-1',
    titulo: 'Escrever os textos',
    descricao: 'Página inicial',
    estado: 'POR_FAZER',
    prioridade: 'MEDIA',
    responsaveis: [],
    criador: { id: ana.id, nome: ana.nome },
    dataLimite: null,
    atrasada: false,
    totalComentarios: 0,
    criadoEm: '2026-10-01T09:00:00Z',
    atualizadoEm: '2026-10-01T09:00:00Z',
    ...alteracoes,
  }
}

import { describe, expect, it } from 'vitest'
import { umProjeto, umaTarefa } from '../testes/dados'
import { somarContagens } from './projetos'
import { alteracoesDe, type DadosTarefa } from './tarefas'

describe('alteracoesDe', () => {
  const tarefa = umaTarefa()
  const iguais: DadosTarefa = {
    titulo: tarefa.titulo,
    descricao: tarefa.descricao,
    prioridade: tarefa.prioridade,
    dataLimite: tarefa.dataLimite,
    estado: tarefa.estado,
    responsavelIds: [],
  }

  it('não envia nada quando nada mudou', () => {
    expect(alteracoesDe(tarefa, iguais)).toEqual({})
  })

  it('envia só os campos alterados, incluindo limpar a data', () => {
    const comData = umaTarefa({ dataLimite: '2026-11-15' })
    expect(alteracoesDe(comData, { ...iguais, prioridade: 'ALTA', dataLimite: null })).toEqual({
      prioridade: 'ALTA',
      dataLimite: null,
    })
  })
})

describe('somarContagens', () => {
  it('soma as tarefas de todos os projetos', () => {
    const projetos = [
      umProjeto(),
      umProjeto({ id: 'p-2', contagens: { POR_FAZER: 2, EM_CURSO: 0, CONCLUIDA: 5, total: 7 } }),
    ]
    expect(somarContagens(projetos)).toEqual({ POR_FAZER: 3, EM_CURSO: 1, CONCLUIDA: 6, total: 10 })
  })

  it('sem projetos dá zero', () => {
    expect(somarContagens([])).toEqual({ POR_FAZER: 0, EM_CURSO: 0, CONCLUIDA: 0, total: 0 })
  })
})

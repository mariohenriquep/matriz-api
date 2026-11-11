import { describe, expect, it } from 'vitest'
import { umProjeto, umaTarefa } from '../testes/dados'
import { somarContagens } from './projetos'
import { alteracoesDe, filtrosDoUrl, filtrosParaUrl, type DadosTarefa } from './tarefas'

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

describe('filtros no URL', () => {
  it('lê os filtros válidos e ignora valores desconhecidos', () => {
    expect(filtrosDoUrl(new URLSearchParams('estado=EM_CURSO&prioridade=ALTA&responsavelId=7'))).toEqual({
      estado: 'EM_CURSO',
      prioridade: 'ALTA',
      responsavelId: '7',
    })
    expect(filtrosDoUrl(new URLSearchParams('estado=FEITA&prioridade=urgente&responsavelId=%20'))).toEqual({})
    expect(filtrosDoUrl(new URLSearchParams(''))).toEqual({})
  })

  it('ignora um responsável que não é um id numérico (regressão do finding CR-03 da Ficha 04)', () => {
    expect(filtrosDoUrl(new URLSearchParams('responsavelId=abc'))).toEqual({})
    expect(filtrosDoUrl(new URLSearchParams('responsavelId=7abc&estado=CONCLUIDA'))).toEqual({ estado: 'CONCLUIDA' })
    expect(filtrosDoUrl(new URLSearchParams('responsavelId=%207%20'))).toEqual({ responsavelId: '7' })
  })

  it('escreve só os filtros preenchidos e volta a ler o mesmo', () => {
    const filtros = { estado: 'POR_FAZER' as const, responsavelId: '2' }
    const url = filtrosParaUrl(filtros)
    expect(url.toString()).toBe('estado=POR_FAZER&responsavelId=2')
    expect(filtrosDoUrl(url)).toEqual(filtros)
    expect(filtrosParaUrl({}).toString()).toBe('')
  })
})

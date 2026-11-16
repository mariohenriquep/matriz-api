import { createEvent, fireEvent, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { describe, expect, it, vi } from 'vitest'
import { umaTarefa } from '../testes/dados'
import { Quadro } from './Quadro'

const tarefas = [
  umaTarefa({ id: 't-1', titulo: 'Escrever os textos', estado: 'POR_FAZER', responsaveis: [{ id: 'u-1', nome: 'Ana Silva' }] }),
  umaTarefa({ id: 't-2', titulo: 'Desenhar maquetas', estado: 'EM_CURSO', totalComentarios: 1, dataLimite: '2026-11-20' }),
]

function mostrar(props: Partial<Parameters<typeof Quadro>[0]> = {}) {
  const aoMover = vi.fn()
  render(
    <MemoryRouter>
      <Quadro tarefas={tarefas} soLeitura={false} ocupada={null} aoMover={aoMover} {...props} />
    </MemoryRouter>,
  )
  return aoMover
}

/** O jsdom não tem DataTransfer: um objeto simples chega para o arrastar. */
function dadosArrasto() {
  const dados: Record<string, string> = {}
  return {
    setData: (tipo: string, valor: string) => {
      dados[tipo] = valor
    },
    getData: (tipo: string) => dados[tipo] ?? '',
    dropEffect: 'none',
    effectAllowed: 'all',
  }
}

function coluna(nome: string) {
  return screen.getByRole('heading', { name: nome }).closest('section') as HTMLElement
}

function arrastar(cartao: HTMLElement, destino: HTMLElement) {
  const dataTransfer = dadosArrasto()
  fireEvent.dragStart(cartao, { dataTransfer })
  const sobre = createEvent.dragOver(destino, { dataTransfer })
  fireEvent(destino, sobre)
  fireEvent.drop(destino, { dataTransfer })
  fireEvent.dragEnd(cartao, { dataTransfer })
  return sobre
}

describe('Quadro', () => {
  it('mostra cada tarefa na coluna do seu estado, com contadores, datas e iniciais', () => {
    mostrar()
    expect(within(coluna('Por fazer')).getByText('Escrever os textos')).toBeInTheDocument()
    expect(within(coluna('Em curso')).getByText('Desenhar maquetas')).toBeInTheDocument()
    expect(within(coluna('Por fazer')).getByLabelText('1 tarefa(s)')).toHaveTextContent('1')
    expect(within(coluna('Concluída')).getByLabelText('0 tarefa(s)')).toBeInTheDocument()
    expect(within(coluna('Concluída')).getByText('Arrasta tarefas para aqui')).toBeInTheDocument()
    expect(screen.getByText('Até 20/11/2026 · 1 comentário')).toBeInTheDocument()
    expect(screen.getByTitle('Ana Silva')).toHaveTextContent('AS')
  })

  it('arrastar um cartão para outra coluna muda o estado', () => {
    const aoMover = mostrar()
    const cartao = screen.getByText('Escrever os textos').closest('li') as HTMLElement
    expect(cartao).toHaveAttribute('draggable', 'true')

    const sobre = arrastar(cartao, coluna('Concluída'))

    expect(sobre.defaultPrevented).toBe(true)
    expect(aoMover).toHaveBeenCalledWith(tarefas[0], 'CONCLUIDA')
  })

  it('largar na própria coluna não faz nada', () => {
    const aoMover = mostrar()
    const cartao = screen.getByText('Escrever os textos').closest('li') as HTMLElement
    arrastar(cartao, coluna('Por fazer'))
    expect(aoMover).not.toHaveBeenCalled()
  })

  it('sem rato, o seletor "Mover para" muda o estado', async () => {
    const aoMover = mostrar()
    await userEvent.setup().selectOptions(screen.getByLabelText('Mover "Desenhar maquetas" para'), 'CONCLUIDA')
    expect(aoMover).toHaveBeenCalledWith(tarefas[1], 'CONCLUIDA')
  })

  it('só de leitura: não se arrasta, não há seletor e as colunas não aceitam cartões', () => {
    const aoMover = mostrar({ soLeitura: true })
    const cartao = screen.getByText('Escrever os textos').closest('li') as HTMLElement
    expect(cartao).toHaveAttribute('draggable', 'false')
    expect(screen.queryByLabelText(/Mover/)).not.toBeInTheDocument()
    expect(within(coluna('Concluída')).getByText('Sem tarefas')).toBeInTheDocument()

    const sobre = arrastar(cartao, coluna('Concluída'))
    expect(sobre.defaultPrevented).toBe(false)
    expect(aoMover).not.toHaveBeenCalled()
  })

  it('o cartão com um pedido em curso não se arrasta nem muda de estado', () => {
    mostrar({ ocupada: 't-1' })
    const cartao = screen.getByText('Escrever os textos').closest('li') as HTMLElement
    expect(cartao).toHaveAttribute('draggable', 'false')
    expect(screen.getByLabelText('Mover "Escrever os textos" para')).toBeDisabled()
  })
})

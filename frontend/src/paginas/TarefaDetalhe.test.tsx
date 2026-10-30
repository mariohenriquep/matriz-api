import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { Utilizador } from '../api/autenticacao'
import * as apiProjetos from '../api/projetos'
import * as apiTarefas from '../api/tarefas'
import { ContextoSessao } from '../auth/contexto'
import { ana, membros, rui, umProjeto, umaTarefa } from '../testes/dados'
import { TarefaDetalhe } from './TarefaDetalhe'

vi.mock('../api/projetos', async (original) => ({
  ...(await original<typeof apiProjetos>()),
  obterProjeto: vi.fn(),
  listarMembros: vi.fn(),
}))
vi.mock('../api/tarefas', async (original) => ({
  ...(await original<typeof apiTarefas>()),
  obterTarefa: vi.fn(),
  listarComentarios: vi.fn(),
  editarTarefa: vi.fn(),
  eliminarTarefa: vi.fn(),
}))

function abrir(utilizador: Utilizador, papel: 'GESTOR' | 'MEMBRO') {
  vi.mocked(apiProjetos.obterProjeto).mockResolvedValue(umProjeto({ meuPapel: papel }))
  render(
    <ContextoSessao.Provider value={{ utilizador, aviso: null, entrar: vi.fn(), sair: vi.fn() }}>
      <MemoryRouter initialEntries={['/tarefas/t-1']}>
        <Routes>
          <Route path="/tarefas/:tarefaId" element={<TarefaDetalhe />} />
          <Route path="/projetos/:projetoId" element={<p>Página do projeto</p>} />
        </Routes>
      </MemoryRouter>
    </ContextoSessao.Provider>,
  )
}

beforeEach(() => {
  vi.mocked(apiTarefas.obterTarefa).mockResolvedValue(umaTarefa({ criador: { id: ana.id, nome: ana.nome } }))
  vi.mocked(apiProjetos.listarMembros).mockResolvedValue({ itens: membros, total: membros.length })
  vi.mocked(apiTarefas.listarComentarios).mockResolvedValue({ itens: [], total: 0 })
})

describe('TarefaDetalhe', () => {
  it('um membro que não criou a tarefa não vê "Eliminar"', async () => {
    abrir(rui, 'MEMBRO')

    expect(await screen.findByRole('heading', { name: 'Escrever os textos' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Editar' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Eliminar' })).not.toBeInTheDocument()
  })

  it('mudar o estado guarda só esse campo e mostra o novo estado', async () => {
    vi.mocked(apiTarefas.editarTarefa).mockResolvedValue(umaTarefa({ estado: 'CONCLUIDA' }))
    abrir(rui, 'MEMBRO')

    await userEvent.selectOptions(await screen.findByLabelText('Estado'), 'CONCLUIDA')

    expect(apiTarefas.editarTarefa).toHaveBeenCalledWith('t-1', { estado: 'CONCLUIDA' })
    expect(await screen.findByText('A tarefa passou para Concluída.')).toBeInTheDocument()
  })

  it('o criador elimina depois de confirmar e volta ao projeto', async () => {
    vi.mocked(apiTarefas.eliminarTarefa).mockResolvedValue(undefined)
    abrir(ana, 'MEMBRO')

    await userEvent.click(await screen.findByRole('button', { name: 'Eliminar' }))
    expect(apiTarefas.eliminarTarefa).not.toHaveBeenCalled()
    expect(screen.getByText('Eliminar a tarefa e os comentários?')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Eliminar' }))

    expect(apiTarefas.eliminarTarefa).toHaveBeenCalledWith('t-1')
    expect(await screen.findByText('Página do projeto')).toBeInTheDocument()
  })

  it('se eliminar falhar, fica na tarefa e mostra o erro', async () => {
    vi.mocked(apiTarefas.eliminarTarefa).mockRejectedValue(new Error('Não tens permissão para eliminar esta tarefa.'))
    abrir(ana, 'GESTOR')

    await userEvent.click(await screen.findByRole('button', { name: 'Eliminar' }))
    await userEvent.click(screen.getByRole('button', { name: 'Eliminar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Não tens permissão para eliminar esta tarefa.')
    expect(screen.getByRole('heading', { name: 'Escrever os textos' })).toBeInTheDocument()
  })
})

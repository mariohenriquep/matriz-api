import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as apiProjetos from '../api/projetos'
import * as apiTarefas from '../api/tarefas'
import { ContextoSessao } from '../auth/contexto'
import { ana, membros, umProjeto, umaTarefa } from '../testes/dados'
import { Inicio } from './Inicio'
import { ProjetoDetalhe } from './ProjetoDetalhe'

vi.mock('../api/projetos', async (original) => ({
  ...(await original<typeof apiProjetos>()),
  obterProjeto: vi.fn(),
  listarMembros: vi.fn(),
  listarProjetos: vi.fn(),
  eliminarProjeto: vi.fn(),
}))
vi.mock('../api/tarefas', async (original) => ({
  ...(await original<typeof apiTarefas>()),
  listarTarefas: vi.fn(),
}))

/** Mostra o endereço atual, para os testes confirmarem os filtros no URL. */
function Endereco() {
  const { search } = useLocation()
  return <output aria-label="endereço">{search}</output>
}

function abrir(endereco: string) {
  render(
    <ContextoSessao.Provider value={{ utilizador: ana, aviso: null, entrar: vi.fn(), sair: vi.fn() }}>
      <MemoryRouter initialEntries={[endereco]}>
        <Routes>
          <Route path="/" element={<Inicio />} />
          <Route
            path="/projetos/:projetoId"
            element={
              <>
                <ProjetoDetalhe />
                <Endereco />
              </>
            }
          />
        </Routes>
      </MemoryRouter>
    </ContextoSessao.Provider>,
  )
}

beforeEach(() => {
  vi.mocked(apiProjetos.obterProjeto).mockResolvedValue(umProjeto())
  vi.mocked(apiProjetos.listarMembros).mockResolvedValue({ itens: membros, total: membros.length })
  vi.mocked(apiProjetos.listarProjetos).mockResolvedValue({ itens: [], total: 0 })
  vi.mocked(apiTarefas.listarTarefas).mockResolvedValue({ itens: [umaTarefa()], total: 1 })
})

describe('ProjetoDetalhe', () => {
  it('aplica os filtros que vêm no URL', async () => {
    abrir('/projetos/p-1?estado=POR_FAZER&prioridade=ALTA')

    expect(await screen.findByText('Escrever os textos')).toBeInTheDocument()
    expect(apiTarefas.listarTarefas).toHaveBeenCalledWith('p-1', { estado: 'POR_FAZER', prioridade: 'ALTA' })
    const filtros = screen.getByRole('group', { name: 'Filtrar tarefas' })
    expect(filtros.querySelector('select')).toHaveValue('POR_FAZER')
  })

  it('guarda no URL o filtro escolhido e limpa-o com "Limpar filtros"', async () => {
    const utilizador = userEvent.setup()
    abrir('/projetos/p-1')
    await screen.findByText('Escrever os textos')

    const filtros = screen.getByRole('group', { name: 'Filtrar tarefas' })
    await utilizador.selectOptions(filtros.querySelectorAll('select')[1], 'ALTA')

    expect(screen.getByLabelText('endereço')).toHaveTextContent('?prioridade=ALTA')
    expect(apiTarefas.listarTarefas).toHaveBeenLastCalledWith('p-1', { prioridade: 'ALTA' })

    await utilizador.click(await screen.findByRole('button', { name: 'Limpar filtros' }))
    expect(screen.getByLabelText('endereço')).toBeEmptyDOMElement()
  })

  it('depois de eliminar o projeto, o painel mostra a confirmação', async () => {
    const utilizador = userEvent.setup()
    vi.mocked(apiProjetos.eliminarProjeto).mockResolvedValue(undefined)
    abrir('/projetos/p-1')
    await screen.findByText('Escrever os textos')

    await utilizador.click(screen.getAllByRole('button', { name: 'Eliminar' })[0])
    await utilizador.click(screen.getByRole('button', { name: 'Eliminar definitivamente' }))

    expect(await screen.findByText('Projeto "Website" eliminado.')).toBeInTheDocument()
    expect(apiProjetos.eliminarProjeto).toHaveBeenCalledWith('p-1')
  })

  it('se eliminar falhar, fica no projeto com o erro e sem mensagem de sucesso', async () => {
    const utilizador = userEvent.setup()
    vi.mocked(apiProjetos.eliminarProjeto).mockRejectedValue(new Error('Sem ligação'))
    abrir('/projetos/p-1')
    await screen.findByText('Escrever os textos')

    await utilizador.click(screen.getAllByRole('button', { name: 'Eliminar' })[0])
    await utilizador.click(screen.getByRole('button', { name: 'Eliminar definitivamente' }))

    expect(await screen.findByText('Sem ligação')).toBeInTheDocument()
    expect(screen.queryByText(/eliminado\./)).not.toBeInTheDocument()
  })
})

import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes, useLocation } from 'react-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import * as apiProjetos from '../api/projetos'
import * as apiTarefas from '../api/tarefas'
import { ContextoSessao } from '../auth/contexto'
import { ana, umProjeto, umaTarefa } from '../testes/dados'
import { Inicio } from './Inicio'

vi.mock('../api/projetos', async (original) => ({
  ...(await original<typeof apiProjetos>()),
  listarProjetos: vi.fn(),
}))
vi.mock('../api/tarefas', async (original) => ({
  ...(await original<typeof apiTarefas>()),
  listarTodasAsTarefas: vi.fn(),
  editarTarefa: vi.fn(),
}))

function Endereco() {
  const { search } = useLocation()
  return <output aria-label="endereço">{search}</output>
}

function abrir(endereco: string) {
  render(
    <ContextoSessao.Provider value={{ utilizador: ana, aviso: null, entrar: vi.fn(), sair: vi.fn() }}>
      <MemoryRouter initialEntries={[endereco]}>
        <Routes>
          <Route
            path="/"
            element={
              <>
                <Inicio />
                <Endereco />
              </>
            }
          />
        </Routes>
      </MemoryRouter>
    </ContextoSessao.Provider>,
  )
}

function coluna(nome: string) {
  return screen.getByRole('heading', { name: nome }).closest('section') as HTMLElement
}

const website = umProjeto({ id: 'p-1', nome: 'Website' })
const formacao = umProjeto({ id: 'p-2', nome: 'Formação', meuPapel: 'MEMBRO' })
const textos = umaTarefa({ id: 't-1', projetoId: 'p-1', titulo: 'Escrever os textos', estado: 'POR_FAZER' })
const sessao = umaTarefa({ id: 't-2', projetoId: 'p-2', titulo: 'Preparar a sessão', estado: 'EM_CURSO' })

describe('Quadro geral na página inicial', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(apiProjetos.listarProjetos).mockResolvedValue({ itens: [website, formacao], total: 2 })
    vi.mocked(apiTarefas.listarTodasAsTarefas).mockResolvedValue({ itens: [textos, sessao], total: 2 })
  })

  it('mostra as tarefas de todos os projetos, com o nome do projeto em cada cartão', async () => {
    abrir('/?vista=quadro')

    expect(await screen.findByText('Escrever os textos')).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: 'Quadro geral' })).toHaveAttribute('aria-selected', 'true')
    expect(within(coluna('Por fazer')).getByRole('link', { name: 'Website' })).toHaveAttribute('href', '/projetos/p-1')
    expect(within(coluna('Em curso')).getByText('Preparar a sessão')).toBeInTheDocument()
    expect(within(coluna('Em curso')).getByRole('link', { name: 'Formação' })).toBeInTheDocument()
    expect(apiTarefas.listarTodasAsTarefas).toHaveBeenCalledWith({})
    // A pesquisa de projetos não se aplica ao quadro
    expect(screen.queryByPlaceholderText('Pesquisar projetos…')).not.toBeInTheDocument()
  })

  it('o separador muda o URL e volta aos projetos', async () => {
    const utilizador = userEvent.setup()
    abrir('/')
    expect(await screen.findByRole('link', { name: 'Website' })).toBeInTheDocument()

    await utilizador.click(screen.getByRole('tab', { name: 'Quadro geral' }))
    expect(await screen.findByText('Escrever os textos')).toBeInTheDocument()
    expect(screen.getByLabelText('endereço')).toHaveTextContent('?vista=quadro')

    await utilizador.click(screen.getByRole('tab', { name: 'Ativos' }))
    expect(await screen.findByPlaceholderText('Pesquisar projetos…')).toBeInTheDocument()
    expect(screen.queryByText('Escrever os textos')).not.toBeInTheDocument()
  })

  it('filtra por projeto, prioridade e só as minhas tarefas', async () => {
    const utilizador = userEvent.setup()
    abrir('/?vista=quadro')
    await screen.findByText('Escrever os textos')

    await utilizador.selectOptions(screen.getByLabelText('Projeto'), 'p-2')
    expect(apiTarefas.listarTodasAsTarefas).toHaveBeenLastCalledWith({ projetoId: 'p-2' })

    await utilizador.selectOptions(screen.getByLabelText('Prioridade'), 'ALTA')
    expect(apiTarefas.listarTodasAsTarefas).toHaveBeenLastCalledWith({ projetoId: 'p-2', prioridade: 'ALTA' })

    await utilizador.click(screen.getByLabelText('Só as minhas'))
    expect(apiTarefas.listarTodasAsTarefas).toHaveBeenLastCalledWith({
      projetoId: 'p-2',
      prioridade: 'ALTA',
      responsavelId: ana.id,
    })
  })

  it('sem tarefas, diz se é por causa dos filtros', async () => {
    const utilizador = userEvent.setup()
    vi.mocked(apiTarefas.listarTodasAsTarefas).mockResolvedValue({ itens: [], total: 0 })
    abrir('/?vista=quadro')
    expect(await screen.findByText(/Ainda não há tarefas nos teus projetos ativos/)).toBeInTheDocument()

    await utilizador.click(screen.getByLabelText('Só as minhas'))
    expect(await screen.findByText('Nenhuma tarefa corresponde aos filtros.')).toBeInTheDocument()
  })

  it('mover uma tarefa muda o estado e atualiza o resumo', async () => {
    const utilizador = userEvent.setup()
    vi.mocked(apiTarefas.editarTarefa).mockResolvedValue({ ...textos, estado: 'CONCLUIDA' })
    abrir('/?vista=quadro')
    await screen.findByText('Escrever os textos')
    vi.mocked(apiProjetos.listarProjetos).mockClear()

    await utilizador.selectOptions(screen.getByLabelText('Mover "Escrever os textos" para'), 'CONCLUIDA')

    expect(await screen.findByText('"Escrever os textos" passou para Concluída.')).toBeInTheDocument()
    expect(apiTarefas.editarTarefa).toHaveBeenCalledWith('t-1', { estado: 'CONCLUIDA' })
    expect(within(coluna('Concluída')).getByText('Escrever os textos')).toBeInTheDocument()
    expect(apiProjetos.listarProjetos).toHaveBeenCalledWith('', false)
  })

  it('se a API recusar, o cartão volta à coluna de origem', async () => {
    const utilizador = userEvent.setup()
    vi.mocked(apiTarefas.editarTarefa).mockRejectedValue(new Error('O projeto foi arquivado.'))
    abrir('/?vista=quadro')
    await screen.findByText('Escrever os textos')

    await utilizador.selectOptions(screen.getByLabelText('Mover "Escrever os textos" para'), 'EM_CURSO')

    expect(await screen.findByText('O projeto foi arquivado.')).toBeInTheDocument()
    expect(within(coluna('Por fazer')).getByText('Escrever os textos')).toBeInTheDocument()
  })
})

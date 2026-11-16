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
  arquivarProjeto: vi.fn(),
  reativarProjeto: vi.fn(),
}))
vi.mock('../api/tarefas', async (original) => ({
  ...(await original<typeof apiTarefas>()),
  listarTarefas: vi.fn(),
  editarTarefa: vi.fn(),
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
          <Route
            path="/"
            element={
              <>
                <Inicio />
                <Endereco />
              </>
            }
          />
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
  it('na vista em lista, aplica os filtros que vêm no URL', async () => {
    abrir('/projetos/p-1?estado=POR_FAZER&prioridade=ALTA&vista=lista')

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
    // No quadro (vista por omissão) não há filtro de estado: o primeiro é a prioridade
    await utilizador.selectOptions(filtros.querySelectorAll('select')[0], 'ALTA')

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

  it('o quadro ignora o filtro de estado do URL e mostra as três colunas', async () => {
    abrir('/projetos/p-1?estado=POR_FAZER')

    expect(await screen.findByText('Escrever os textos')).toBeInTheDocument()
    expect(apiTarefas.listarTarefas).toHaveBeenCalledWith('p-1', {})
    for (const coluna of ['Por fazer', 'Em curso', 'Concluída']) {
      expect(screen.getByRole('heading', { name: coluna })).toBeInTheDocument()
    }
  })

  it('alterna entre quadro e lista e guarda a escolha no URL', async () => {
    const utilizador = userEvent.setup()
    abrir('/projetos/p-1')
    await screen.findByText('Escrever os textos')

    await utilizador.click(screen.getByRole('button', { name: 'Lista' }))
    expect(screen.getByLabelText('endereço')).toHaveTextContent('?vista=lista')
    expect(screen.getByRole('button', { name: 'Lista' })).toHaveAttribute('aria-pressed', 'true')
    expect(await screen.findByRole('button', { name: 'Editar' })).toBeInTheDocument()

    await utilizador.click(screen.getByRole('button', { name: 'Quadro' }))
    expect(screen.getByLabelText('endereço')).toBeEmptyDOMElement()
  })

  it('o gestor arquiva o projeto: aparece o aviso e deixa de poder criar tarefas', async () => {
    const utilizador = userEvent.setup()
    vi.mocked(apiProjetos.arquivarProjeto).mockResolvedValue(umProjeto({ arquivado: true, arquivadoEm: '2026-11-16T10:00:00Z' }))
    abrir('/projetos/p-1')
    await screen.findByText('Escrever os textos')
    expect(screen.getByRole('button', { name: 'Nova tarefa' })).toBeInTheDocument()

    await utilizador.click(screen.getByRole('button', { name: 'Arquivar' }))

    expect(apiProjetos.arquivarProjeto).toHaveBeenCalledWith('p-1')
    expect(await screen.findByText(/Este projeto está arquivado/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Nova tarefa' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Editar' })).not.toBeInTheDocument()
    expect(screen.queryByLabelText(/Adicionar por email/)).not.toBeInTheDocument()
    expect(screen.queryByLabelText(/Mover "Escrever os textos" para/)).not.toBeInTheDocument()
  })

  it('num projeto arquivado o gestor pode reativar e um membro só consulta', async () => {
    const utilizador = userEvent.setup()
    vi.mocked(apiProjetos.obterProjeto).mockResolvedValue(umProjeto({ arquivado: true }))
    vi.mocked(apiProjetos.reativarProjeto).mockResolvedValue(umProjeto())
    abrir('/projetos/p-1')
    await screen.findByText('Escrever os textos')

    await utilizador.click(screen.getByRole('button', { name: 'Reativar projeto' }))

    expect(apiProjetos.reativarProjeto).toHaveBeenCalledWith('p-1')
    expect(await screen.findByText('Projeto reativado.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Nova tarefa' })).toBeInTheDocument()
  })

  it('um membro vê o aviso de arquivo sem botão para reativar', async () => {
    vi.mocked(apiProjetos.obterProjeto).mockResolvedValue(umProjeto({ arquivado: true, meuPapel: 'MEMBRO' }))
    abrir('/projetos/p-1')
    await screen.findByText('Escrever os textos')

    expect(screen.getByText(/Este projeto está arquivado/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Reativar projeto' })).not.toBeInTheDocument()
  })

  it('mover no quadro muda logo de coluna e volta atrás se a API recusar', async () => {
    const utilizador = userEvent.setup()
    vi.mocked(apiTarefas.editarTarefa).mockRejectedValue(new Error('O projeto está arquivado'))
    abrir('/projetos/p-1')
    await screen.findByText('Escrever os textos')

    await utilizador.selectOptions(screen.getByLabelText('Mover "Escrever os textos" para'), 'CONCLUIDA')

    expect(apiTarefas.editarTarefa).toHaveBeenCalledWith('t-1', { estado: 'CONCLUIDA' })
    expect(await screen.findByText('O projeto está arquivado')).toBeInTheDocument()
    const porFazer = screen.getByRole('heading', { name: 'Por fazer' }).closest('section') as HTMLElement
    expect(porFazer).toHaveTextContent('Escrever os textos')
  })

  it('mover no quadro com sucesso atualiza as contagens do projeto', async () => {
    const utilizador = userEvent.setup()
    vi.mocked(apiTarefas.editarTarefa).mockResolvedValue(umaTarefa({ estado: 'EM_CURSO' }))
    abrir('/projetos/p-1')
    await screen.findByText('Escrever os textos')
    vi.mocked(apiProjetos.obterProjeto).mockClear()

    await utilizador.selectOptions(screen.getByLabelText('Mover "Escrever os textos" para'), 'EM_CURSO')

    expect(await screen.findByText('"Escrever os textos" passou para Em curso.')).toBeInTheDocument()
    const emCurso = screen.getByRole('heading', { name: 'Em curso' }).closest('section') as HTMLElement
    expect(emCurso).toHaveTextContent('Escrever os textos')
    expect(apiProjetos.obterProjeto).toHaveBeenCalledWith('p-1')
  })

  it('no painel, o separador Arquivados pede só os projetos arquivados', async () => {
    const utilizador = userEvent.setup()
    vi.mocked(apiProjetos.listarProjetos).mockImplementation(async (_pesquisa, arquivados) =>
      arquivados
        ? { itens: [umProjeto({ nome: 'Antigo', arquivado: true, arquivadoEm: '2026-11-01T10:00:00Z' })], total: 1 }
        : { itens: [umProjeto()], total: 1 },
    )
    abrir('/')
    expect(await screen.findByRole('link', { name: 'Website' })).toBeInTheDocument()

    await utilizador.click(screen.getByRole('tab', { name: 'Arquivados' }))

    expect(await screen.findByRole('link', { name: 'Antigo' })).toBeInTheDocument()
    expect(screen.getByText('Arquivado')).toBeInTheDocument()
    expect(screen.getByLabelText('endereço')).toHaveTextContent('?vista=arquivados')
    expect(apiProjetos.listarProjetos).toHaveBeenLastCalledWith('', true)
    // O resumo continua a contar só os projetos ativos
    expect(screen.getByText(/Participas em 1 projeto\./)).toBeInTheDocument()
  })

  it('sem projetos arquivados, explica como arquivar', async () => {
    abrir('/?vista=arquivados')
    expect(await screen.findByText(/Não há projetos arquivados/)).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: 'Arquivados' })).toHaveAttribute('aria-selected', 'true')
  })
})

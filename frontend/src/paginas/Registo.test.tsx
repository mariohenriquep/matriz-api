import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { MemoryRouter, Route, Routes } from 'react-router'
import * as apiAutenticacao from '../api/autenticacao'
import { ErroApi } from '../api/cliente'
import { ContextoSessao } from '../auth/contexto'
import { Login } from './Login'
import { Registo } from './Registo'

vi.mock('../api/autenticacao', async (original) => ({
  ...(await original<typeof apiAutenticacao>()),
  registar: vi.fn(),
}))

const MENSAGEM = 'Pedido de registo recebido. Se este email ainda não tinha conta, a conta foi criada e já podes iniciar sessão.'

function abrir() {
  render(
    <ContextoSessao.Provider value={{ utilizador: null, aviso: null, entrar: vi.fn(), sair: vi.fn() }}>
      <MemoryRouter initialEntries={['/registo']}>
        <Routes>
          <Route path="/registo" element={<Registo />} />
          <Route
            path="/login"
            element={<Login />}
          />
        </Routes>
      </MemoryRouter>
    </ContextoSessao.Provider>,
  )
}

async function preencher(dados: { nome?: string; email?: string; password?: string; confirmacao?: string }) {
  const utilizador = userEvent.setup()
  if (dados.nome) await utilizador.type(screen.getByLabelText('Nome'), dados.nome)
  if (dados.email) await utilizador.type(screen.getByLabelText('Email'), dados.email)
  if (dados.password) await utilizador.type(screen.getByLabelText('Password'), dados.password)
  if (dados.confirmacao) await utilizador.type(screen.getByLabelText('Confirmar password'), dados.confirmacao)
  await utilizador.click(screen.getByRole('button', { name: 'Criar conta' }))
}

describe('Criar conta', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('cria a conta e segue para o login com a mensagem do servidor e o email preenchido', async () => {
    vi.mocked(apiAutenticacao.registar).mockResolvedValue({ mensagem: MENSAGEM })
    abrir()
    await preencher({ nome: ' Nova Pessoa ', email: 'nova@exemplo.pt', password: 'uma password segura', confirmacao: 'uma password segura' })

    expect(apiAutenticacao.registar).toHaveBeenCalledWith({
      nome: 'Nova Pessoa',
      email: 'nova@exemplo.pt',
      password: 'uma password segura',
      site: '',
    })
    expect(await screen.findByText(MENSAGEM)).toHaveAttribute('role', 'status')
    expect(screen.getByText('Inicia sessão para veres os teus projetos.')).toBeInTheDocument()
    expect(screen.getByLabelText('Email')).toHaveValue('nova@exemplo.pt')
  })

  it('valida antes de enviar: campos vazios, email, password curta e confirmação diferente', async () => {
    abrir()
    await preencher({ email: 'nova@exemplo', password: 'curta', confirmacao: 'outra' })

    expect(screen.getByText('O nome é obrigatório')).toBeInTheDocument()
    expect(screen.getByText('O email não é válido')).toBeInTheDocument()
    expect(screen.getByText('A password tem de ter pelo menos 8 caracteres')).toBeInTheDocument()
    expect(screen.getByText('As passwords não coincidem')).toBeInTheDocument()
    expect(screen.getByLabelText('Nome')).toHaveAttribute('aria-invalid', 'true')
    expect(apiAutenticacao.registar).not.toHaveBeenCalled()
  })

  it('mostra no campo certo o erro do servidor (ex.: password comum)', async () => {
    const mensagem = 'Esta password é demasiado comum. Escolhe outra'
    vi.mocked(apiAutenticacao.registar).mockRejectedValue(new ErroApi(400, mensagem, [{ campo: 'password', mensagem }]))
    abrir()
    await preencher({ nome: 'Nova', email: 'nova@exemplo.pt', password: 'password123', confirmacao: 'password123' })

    expect(await screen.findByRole('alert')).toHaveTextContent(mensagem)
    expect(screen.getByLabelText('Password')).toHaveAttribute('aria-invalid', 'true')
    expect(screen.getByRole('button', { name: 'Criar conta' })).toBeEnabled()
  })

  it('mostra o limite de pedidos do servidor', async () => {
    vi.mocked(apiAutenticacao.registar).mockRejectedValue(
      new ErroApi(429, 'Demasiadas tentativas. Tenta novamente daqui a 60 minutos.'),
    )
    abrir()
    await preencher({ nome: 'Nova', email: 'nova@exemplo.pt', password: 'uma password segura', confirmacao: 'uma password segura' })

    expect(await screen.findByRole('alert')).toHaveTextContent('Demasiadas tentativas')
  })

  it('o campo-armadilha para robôs fica escondido das pessoas e do teclado', () => {
    abrir()
    const armadilha = screen.getByLabelText('Site', { selector: 'input' })
    expect(armadilha).toHaveAttribute('tabindex', '-1')
    expect(armadilha.closest('[aria-hidden="true"]')).not.toBeNull()
    expect(screen.getByText(/Já tens conta/).querySelector('a')).toHaveAttribute('href', '/login')
  })
})

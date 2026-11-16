import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { MemoryRouter } from 'react-router'
import { ContextoSessao } from '../auth/contexto'
import { Apresentacao } from './Apresentacao'

describe('Página inicial pública', () => {
  it('apresenta a Matriz e leva a criar conta ou a entrar', () => {
    render(
      <ContextoSessao.Provider value={{ utilizador: null, aviso: null, entrar: vi.fn(), sair: vi.fn() }}>
        <MemoryRouter>
          <Apresentacao />
        </MemoryRouter>
      </ContextoSessao.Provider>,
    )

    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Os projetos da tua equipa')
    expect(screen.getByRole('link', { name: 'Criar conta grátis' })).toHaveAttribute('href', '/registo')
    expect(screen.getByRole('link', { name: 'Já tenho conta' })).toHaveAttribute('href', '/login')
    expect(screen.getByRole('link', { name: 'Entrar' })).toHaveAttribute('href', '/login')
    for (const titulo of ['Quadro Kanban', 'Quadro geral', 'Trabalho em equipa', 'Arquivo de projetos']) {
      expect(screen.getByRole('heading', { name: titulo })).toBeInTheDocument()
    }
    // A conta gratuita não cria projetos: a página diz isso antes do registo
    expect(screen.getByText(/plano Profissional/)).toBeInTheDocument()
  })
})

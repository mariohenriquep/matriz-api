import { render } from '@testing-library/react'
import type { ReactNode } from 'react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { vi } from 'vitest'
import { ContextoSessao } from '../auth/contexto'
import type { Conta } from '../api/autenticacao'
import { contaAna } from './dados'
import { Endereco } from './Endereco'

/** Abre as páginas indicadas (caminho → elemento) no endereço dado, com a Ana (ou outra conta) autenticada. */
export function abrirComSessao(endereco: string, paginas: Record<string, ReactNode>, conta: Conta = contaAna) {
  render(
    <ContextoSessao.Provider value={{ utilizador: conta, aviso: null, entrar: vi.fn(), sair: vi.fn() }}>
      <MemoryRouter initialEntries={[endereco]}>
        <Routes>
          {Object.entries(paginas).map(([caminho, elemento]) => (
            <Route
              key={caminho}
              path={caminho}
              element={
                <>
                  {elemento}
                  <Endereco />
                </>
              }
            />
          ))}
        </Routes>
      </MemoryRouter>
    </ContextoSessao.Provider>,
  )
}

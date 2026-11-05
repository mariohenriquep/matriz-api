import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { Progresso } from './Contagens'

describe('Progresso', () => {
  it('mostra a percentagem arredondada e o número de tarefas concluídas', () => {
    render(<Progresso contagens={{ POR_FAZER: 1, EM_CURSO: 1, CONCLUIDA: 1, total: 3 }} />)

    expect(screen.getByText('33% concluído · 1 de 3 tarefas')).toBeInTheDocument()
    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '33')
  })

  it('num projeto sem tarefas não divide por zero', () => {
    render(<Progresso contagens={{ POR_FAZER: 0, EM_CURSO: 0, CONCLUIDA: 0, total: 0 }} />)

    expect(screen.getByText('Ainda sem tarefas')).toBeInTheDocument()
    expect(screen.getByRole('progressbar')).toHaveAttribute('aria-valuenow', '0')
  })
})

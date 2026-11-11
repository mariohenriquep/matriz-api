import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ErroApi } from '../api/cliente'
import { LIMITE_TITULO } from '../api/tarefas'
import { membros, umaTarefa } from '../testes/dados'
import { FormularioTarefa } from './FormularioTarefa'

describe('FormularioTarefa', () => {
  it('não envia sem título e mostra o erro junto ao campo', async () => {
    const aoSubmeter = vi.fn()
    render(<FormularioTarefa membros={membros} aoSubmeter={aoSubmeter} aoCancelar={() => {}} />)

    await userEvent.type(screen.getByLabelText('Título'), '   ')
    await userEvent.click(screen.getByRole('button', { name: 'Criar tarefa' }))

    expect(screen.getByText('O título é obrigatório')).toBeInTheDocument()
    expect(screen.getByRole('textbox', { name: /^Título/ })).toHaveAttribute('aria-invalid', 'true')
    expect(aoSubmeter).not.toHaveBeenCalled()
  })

  it('não envia um título acima do limite do contrato', async () => {
    const aoSubmeter = vi.fn()
    render(<FormularioTarefa membros={membros} aoSubmeter={aoSubmeter} aoCancelar={() => {}} />)

    await userEvent.click(screen.getByLabelText('Título'))
    await userEvent.paste('a'.repeat(LIMITE_TITULO + 1))
    await userEvent.click(screen.getByRole('button', { name: 'Criar tarefa' }))

    expect(screen.getByText(`O título tem no máximo ${LIMITE_TITULO} caracteres`)).toBeInTheDocument()
    expect(aoSubmeter).not.toHaveBeenCalled()
  })

  it('aceita um título com o limite em emojis, que contam um caracter cada (regressão da Ficha 04, Ex. 2)', async () => {
    const aoSubmeter = vi.fn().mockResolvedValue(undefined)
    render(<FormularioTarefa membros={membros} aoSubmeter={aoSubmeter} aoCancelar={() => {}} />)

    await userEvent.click(screen.getByLabelText('Título'))
    await userEvent.paste('📌'.repeat(LIMITE_TITULO))
    await userEvent.click(screen.getByRole('button', { name: 'Criar tarefa' }))

    expect(screen.queryByText(`O título tem no máximo ${LIMITE_TITULO} caracteres`)).not.toBeInTheDocument()
    expect(aoSubmeter).toHaveBeenCalledWith(expect.objectContaining({ titulo: '📌'.repeat(LIMITE_TITULO) }))
  })

  it('envia os dados limpos e com os responsáveis escolhidos', async () => {
    const aoSubmeter = vi.fn().mockResolvedValue(undefined)
    render(<FormularioTarefa membros={membros} aoSubmeter={aoSubmeter} aoCancelar={() => {}} />)

    await userEvent.type(screen.getByLabelText('Título'), '  Rever o logótipo  ')
    await userEvent.click(screen.getByLabelText('Rui Membro'))
    await userEvent.click(screen.getByRole('button', { name: 'Criar tarefa' }))

    expect(aoSubmeter).toHaveBeenCalledWith({
      titulo: 'Rever o logótipo',
      descricao: null,
      prioridade: 'MEDIA',
      dataLimite: null,
      estado: 'POR_FAZER',
      responsavelIds: ['u-rui'],
    })
  })

  it('mantém os dados e mostra o erro do servidor quando o pedido falha', async () => {
    const aoSubmeter = vi.fn().mockRejectedValue(new ErroApi(503, 'O servidor está indisponível.'))
    render(<FormularioTarefa membros={membros} aoSubmeter={aoSubmeter} aoCancelar={() => {}} />)

    await userEvent.type(screen.getByLabelText('Título'), 'Preparar a reunião')
    await userEvent.type(screen.getByLabelText(/Descrição/), 'Com a equipa toda')
    await userEvent.click(screen.getByRole('button', { name: 'Criar tarefa' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('O servidor está indisponível.')
    expect(screen.getByLabelText('Título')).toHaveValue('Preparar a reunião')
    expect(screen.getByLabelText(/Descrição/)).toHaveValue('Com a equipa toda')
    expect(screen.getByRole('button', { name: 'Criar tarefa' })).toBeEnabled()
  })

  it('mostra os erros de campo devolvidos pela API', async () => {
    const erro = new ErroApi(400, 'Dados inválidos', [{ campo: 'dataLimite', mensagem: 'Data inválida' }])
    const aoSubmeter = vi.fn().mockRejectedValue(erro)
    render(<FormularioTarefa membros={membros} aoSubmeter={aoSubmeter} aoCancelar={() => {}} />)

    await userEvent.type(screen.getByLabelText('Título'), 'Tarefa')
    await userEvent.click(screen.getByRole('button', { name: 'Criar tarefa' }))

    expect(await screen.findByText('Data inválida')).toBeInTheDocument()
    expect(screen.getByLabelText(/Data limite/)).toHaveAttribute('aria-invalid', 'true')
  })

  it('não repete o envio enquanto o pedido está em curso', async () => {
    const aoSubmeter = vi.fn(() => new Promise<void>(() => {}))
    render(<FormularioTarefa membros={membros} aoSubmeter={aoSubmeter} aoCancelar={() => {}} />)

    await userEvent.type(screen.getByLabelText('Título'), 'Tarefa')
    await userEvent.click(screen.getByRole('button', { name: 'Criar tarefa' }))
    expect(screen.getByRole('button', { name: /A guardar/ })).toBeDisabled()
    // Mesmo que o formulário volte a ser submetido (Enter, duplo clique), só sai um pedido
    fireEvent.submit(screen.getByLabelText('Título').closest('form')!)

    expect(aoSubmeter).toHaveBeenCalledTimes(1)
  })

  it('na edição começa com os valores da tarefa e permite mudar o estado', async () => {
    const aoSubmeter = vi.fn().mockResolvedValue(undefined)
    const tarefa = umaTarefa({ prioridade: 'ALTA', dataLimite: '2026-11-15' })
    render(<FormularioTarefa inicial={tarefa} membros={membros} aoSubmeter={aoSubmeter} aoCancelar={() => {}} />)

    expect(screen.getByLabelText('Título')).toHaveValue('Escrever os textos')
    await userEvent.selectOptions(screen.getByLabelText('Estado'), 'EM_CURSO')
    await userEvent.click(screen.getByRole('button', { name: 'Guardar' }))

    expect(aoSubmeter).toHaveBeenCalledWith(
      expect.objectContaining({ estado: 'EM_CURSO', prioridade: 'ALTA', dataLimite: '2026-11-15' }),
    )
  })
})

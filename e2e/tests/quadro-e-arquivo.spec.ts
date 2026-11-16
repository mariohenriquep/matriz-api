import { expect, test } from '@playwright/test'
import { criarProjetoComTarefa, entrar } from './passos'

/**
 * Extras do MVP pela interface: quadro Kanban (arrastar muda o estado e fica guardado) e arquivo de projetos
 * (o projeto sai da lista, fica só de leitura e volta ao ser reativado). Usa um projeto novo para não mexer no seed.
 */
test('a Ana arrasta uma tarefa para Concluída, arquiva o projeto e reativa-o', async ({ page }) => {
  const projeto = `Projeto quadro E2E ${Date.now()}`
  const titulo = 'Preparar a apresentação'

  await entrar(page, 'ana@matriz.local', 'Ana Silva')
  const tarefas = await criarProjetoComTarefa(page, projeto, titulo)

  // Quadro: a tarefa começa em "Por fazer"; arrastá-la para "Concluída"
  const porFazer = tarefas.getByRole('region', { name: 'Por fazer' })
  const concluida = tarefas.getByRole('region', { name: 'Concluída' })
  const cartao = porFazer.getByRole('listitem').filter({ hasText: titulo })
  await expect(cartao).toBeVisible()
  await cartao.dragTo(concluida)
  await expect(page.getByText(`"${titulo}" passou para Concluída.`)).toBeVisible()
  await expect(concluida.getByRole('link', { name: titulo })).toBeVisible()
  await expect(porFazer.getByRole('link', { name: titulo })).toHaveCount(0)

  // Persistência: depois de recarregar continua em "Concluída"
  await page.reload()
  await expect(page.getByRole('region', { name: 'Concluída' }).getByRole('link', { name: titulo })).toBeVisible()

  // Arquivar: aviso, sem criar tarefas, e o projeto passa para o separador "Arquivados"
  await page.getByRole('button', { name: 'Arquivar' }).click()
  await expect(page.getByText(/Este projeto está arquivado/)).toBeVisible()
  await expect(page.getByRole('button', { name: 'Nova tarefa' })).toHaveCount(0)
  await page.getByRole('link', { name: '← Os meus projetos' }).click()
  await expect(page.getByRole('tab', { name: 'Arquivados' })).toHaveAttribute('aria-selected', 'true')
  await expect(page.getByRole('link', { name: projeto })).toBeVisible()
  await page.getByRole('tab', { name: 'Ativos' }).click()
  await expect(page.getByRole('link', { name: 'Website institucional' })).toBeVisible()
  await expect(page.getByRole('link', { name: projeto })).toHaveCount(0)

  // Reativar volta a permitir alterações
  await page.getByRole('tab', { name: 'Arquivados' }).click()
  await page.getByRole('link', { name: projeto }).click()
  await page.getByRole('button', { name: 'Reativar projeto' }).click()
  await expect(page.getByText('Projeto reativado.')).toBeVisible()
  await expect(page.getByRole('button', { name: 'Nova tarefa' })).toBeVisible()
})

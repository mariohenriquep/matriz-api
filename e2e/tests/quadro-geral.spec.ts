import { expect, test } from '@playwright/test'
import { criarProjetoComTarefa, entrar } from './passos'

/**
 * Quadro geral da página inicial: as tarefas de todos os projetos ativos num só Kanban. A Joana vê as tarefas
 * dos projetos de que é membro (seed), filtra por um projeto novo e arrasta uma tarefa sem abrir o projeto.
 */
test('a Joana vê todas as suas tarefas na página inicial e muda uma de estado', async ({ page }) => {
  const projeto = `Projeto geral E2E ${Date.now()}`
  const titulo = 'Rever o orçamento'

  await entrar(page, 'joana@matriz.local', 'Joana Lopes')
  await criarProjetoComTarefa(page, projeto, titulo)

  // Página inicial, separador "Quadro geral": tarefas de vários projetos, com o nome do projeto no cartão
  await page.getByRole('link', { name: 'Quadro geral' }).click()
  await expect(page.getByRole('tab', { name: 'Quadro geral' })).toHaveAttribute('aria-selected', 'true')
  const quadro = page.getByRole('region', { name: 'Todas as tarefas' })
  const porFazer = quadro.getByRole('region', { name: 'Por fazer' })
  const emCurso = quadro.getByRole('region', { name: 'Em curso' })
  await expect(emCurso.getByRole('link', { name: 'Levantar os requisitos com o cliente' })).toBeVisible()
  await expect(emCurso.getByRole('link', { name: 'App móvel' })).toBeVisible()
  await expect(porFazer.getByRole('link', { name: 'Preparar a sessão sobre Git' })).toBeVisible()
  const cartao = porFazer.getByRole('listitem').filter({ hasText: titulo })
  await expect(cartao.getByRole('link', { name: projeto })).toBeVisible()

  // Filtrar por projeto deixa só a tarefa nova
  await quadro.getByLabel('Projeto').selectOption({ label: projeto })
  await expect(quadro.getByRole('listitem')).toHaveCount(1)

  // Arrastar para "Em curso" sem abrir o projeto
  await cartao.dragTo(emCurso)
  await expect(page.getByText(`"${titulo}" passou para Em curso.`)).toBeVisible()
  await expect(emCurso.getByRole('link', { name: titulo })).toBeVisible()

  // No projeto, a tarefa aparece também em "Em curso"
  await emCurso.getByRole('link', { name: projeto }).click()
  await expect(page.getByRole('heading', { level: 1, name: projeto })).toBeVisible()
  await expect(
    page.getByRole('region', { name: 'Tarefas' }).getByRole('region', { name: 'Em curso' }).getByRole('link', { name: titulo }),
  ).toBeVisible()
})

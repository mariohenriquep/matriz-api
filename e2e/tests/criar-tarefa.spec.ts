import { expect, test } from '@playwright/test'
import { PASSWORD_DEMO } from '../ambiente.mjs'

/**
 * T-17 de docs/test-plan.md (US-06 CA1 pela interface): autenticar → abrir projeto → criar tarefa → ver a tarefa na
 * listagem. Seletores pelo papel e pelo nome acessível (o que o utilizador vê), sem esperas fixas: as asserções do
 * Playwright esperam até o elemento aparecer.
 */
test('a Ana entra, abre o Website institucional, cria uma tarefa e vê-a na lista', async ({ page }) => {
  // Título único: o teste não depende de dados de execuções anteriores
  const titulo = `Rever logótipo E2E ${Date.now()}`

  // 1. Autenticar
  await page.goto('/')
  await page.getByLabel('Email').fill('ana@matriz.local')
  await page.getByLabel('Password').fill(PASSWORD_DEMO)
  await page.getByRole('button', { name: 'Entrar' }).click()
  await expect(page.getByRole('heading', { name: 'Olá, Ana Silva' })).toBeVisible()

  // 2. Abrir o projeto
  await page.getByRole('link', { name: 'Website institucional' }).click()
  await expect(page.getByRole('heading', { level: 1, name: 'Website institucional' })).toBeVisible()
  const tarefas = page.getByRole('region', { name: 'Tarefas' })
  await expect(tarefas.getByRole('listitem')).toHaveCount(3) // as 3 tarefas do seed

  // 3. Criar a tarefa
  await tarefas.getByRole('button', { name: 'Nova tarefa' }).click()
  await tarefas.getByLabel('Título').fill(titulo)
  await tarefas.getByRole('button', { name: 'Criar tarefa' }).click()

  // 4. Verificar na listagem, sem recarregar a página
  const nova = tarefas.getByRole('listitem').filter({ has: page.getByRole('link', { name: titulo }) })
  await expect(nova).toBeVisible()
  await expect(nova).toContainText('Média') // prioridade por omissão
  await expect(tarefas.getByRole('listitem')).toHaveCount(4)

  // 5. Persistência: depois de recarregar, a tarefa continua lá (vem do backend, não do estado da página)
  await page.reload()
  await expect(page.getByRole('region', { name: 'Tarefas' }).getByRole('link', { name: titulo })).toBeVisible()
})

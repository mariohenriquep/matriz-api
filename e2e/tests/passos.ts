import { expect, type Page } from '@playwright/test'
import { PASSWORD_DEMO } from '../ambiente.mjs'

/** Entra com uma conta do seed e espera pela saudação do painel. */
export async function entrar(page: Page, email: string, nome: string) {
  // Em "/" quem não tem sessão vê a apresentação pública (versão 0.8.0): o login está em /login
  await page.goto('/login')
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password').fill(PASSWORD_DEMO)
  await page.getByRole('button', { name: 'Entrar' }).click()
  await expect(page.getByRole('heading', { name: `Olá, ${nome}` })).toBeVisible()
}

/**
 * Cria um projeto novo (para não mexer nos dados do seed), abre-o e cria uma tarefa.
 * Devolve a região "Tarefas" da página do projeto.
 */
export async function criarProjetoComTarefa(page: Page, projeto: string, titulo: string) {
  await page.getByRole('button', { name: 'Novo projeto' }).click()
  await page.getByRole('region', { name: 'Novo projeto' }).getByLabel('Nome').fill(projeto)
  await page.getByRole('button', { name: 'Criar projeto' }).click()
  await page.getByRole('link', { name: projeto }).click()
  await expect(page.getByRole('heading', { level: 1, name: projeto })).toBeVisible()
  const tarefas = page.getByRole('region', { name: 'Tarefas' })
  await tarefas.getByRole('button', { name: 'Nova tarefa' }).click()
  await tarefas.getByLabel('Título').fill(titulo)
  await tarefas.getByRole('button', { name: 'Criar tarefa' }).click()
  await expect(tarefas.getByRole('link', { name: titulo })).toBeVisible()
  return tarefas
}

/** Termina a sessão e espera pela página inicial pública. */
export async function terminarSessao(page: Page) {
  await page.getByRole('button', { name: 'Terminar sessão' }).click()
  await expect(page.getByRole('link', { name: 'Criar conta', exact: true })).toBeVisible()
}

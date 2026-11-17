import { expect, test } from '@playwright/test'
import { entrar, terminarSessao } from './passos'

/**
 * Registo público (versão 0.8.0): um visitante vê a apresentação, cria conta e entra. A conta gratuita não cria
 * projetos e começa sem nenhum; depois de a Ana (Gestora) a adicionar, vê o projeto. Registar de novo o mesmo
 * email dá a mesma mensagem (não revela que a conta existe).
 */
test('um visitante cria conta, entra sem projetos e vê o projeto depois de um gestor o adicionar', async ({ page }) => {
  const email = `nova.${Date.now()}@exemplo.pt`
  const password = 'cavalo correto bateria'

  // Página inicial pública
  await page.goto('/')
  await expect(page.getByRole('heading', { level: 1, name: /Os projetos da tua equipa/ })).toBeVisible()
  await page.getByRole('link', { name: 'Criar conta grátis' }).click()

  // Registo
  await page.getByLabel('Nome').fill('Nova Pessoa')
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password', { exact: true }).fill(password)
  await page.getByLabel('Confirmar password').fill(password)
  await page.getByRole('button', { name: 'Criar conta' }).click()
  const mensagem = page.getByText(/Pedido de registo recebido/)
  await expect(mensagem).toBeVisible()
  const textoMensagem = await mensagem.textContent()
  await expect(page.getByLabel('Email')).toHaveValue(email)

  // Entrar: sem projetos e sem o botão de criar
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: 'Entrar' }).click()
  await expect(page.getByRole('heading', { name: 'Olá, Nova Pessoa' })).toBeVisible()
  await expect(page.getByText(/Quando o gestor de um projeto te adicionar/)).toBeVisible()
  await expect(page.getByRole('button', { name: /Novo projeto/ })).toHaveCount(0)
  await terminarSessao(page)

  // Registar outra vez o mesmo email: a mesma mensagem
  await page.goto('/registo')
  await page.getByLabel('Nome').fill('Outra Pessoa')
  await page.getByLabel('Email').fill(email.toUpperCase())
  await page.getByLabel('Password', { exact: true }).fill('outra password longa')
  await page.getByLabel('Confirmar password').fill('outra password longa')
  await page.getByRole('button', { name: 'Criar conta' }).click()
  await expect(page.getByText(/Pedido de registo recebido/)).toHaveText(textoMensagem ?? '')

  // A Ana adiciona a conta nova ao "Website institucional"
  await entrar(page, 'ana@matriz.local', 'Ana Silva')
  await page.getByRole('link', { name: 'Website institucional' }).click()
  await page.getByLabel('Adicionar por email').fill(email)
  await page.getByRole('button', { name: 'Adicionar', exact: true }).click()
  await expect(page.getByRole('heading', { name: 'Membros (3)' })).toBeVisible()
  await terminarSessao(page)

  // A conta nova já vê o projeto, como membro
  await page.goto('/login')
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: 'Entrar' }).click()
  await expect(page.getByRole('button', { name: 'Terminar sessão' })).toBeVisible()
  await page.goto('/')
  const cartao = page.getByRole('listitem').filter({ hasText: 'Website institucional' })
  await expect(cartao.getByText('Gestor: Ana Silva')).toBeVisible()
  await expect(page.getByRole('button', { name: /Novo projeto/ })).toHaveCount(0)
})

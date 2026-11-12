// Cria de novo a base de dados dos testes E2E (matriz_e2e) no PostgreSQL do docker compose.
// Corre antes de o backend arrancar: o Flyway cria o esquema e o seed cria as contas de demonstração.
// Nunca toca na base de dados de desenvolvimento (matriz).
import { execFileSync } from 'node:child_process'
import { lerEnv, BD_E2E } from './ambiente.mjs'

const env = lerEnv()
const contentor = 'matriz-postgres'
const utilizador = env.POSTGRES_USER ?? 'matriz'

function psql(sql) {
  execFileSync('docker', ['exec', contentor, 'psql', '-v', 'ON_ERROR_STOP=1', '-U', utilizador, '-d', 'postgres', '-c', sql], {
    stdio: ['ignore', 'ignore', 'inherit'],
  })
}

try {
  psql(`DROP DATABASE IF EXISTS ${BD_E2E} WITH (FORCE)`)
  psql(`CREATE DATABASE ${BD_E2E}`)
  console.log(`[e2e] Base de dados ${BD_E2E} criada de novo.`)
} catch {
  console.error(`[e2e] Não consegui preparar a base de dados ${BD_E2E}. O PostgreSQL está a correr? (docker compose up -d postgres na raiz)`)
  process.exit(1)
}

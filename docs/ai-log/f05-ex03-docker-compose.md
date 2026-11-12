# Ficha 05 — Exercício 3: Docker Compose e execução da aplicação

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Executar a Matriz completa (PostgreSQL, backend, frontend) com Docker Compose.

# Context
docker-compose.yml existente (só PostgreSQL, porta 5433, volume matriz-dados), imagens do Ex. 2,
.env.example, testes E2E que usam o container matriz-postgres.

# Constraints
- compose.yaml no lugar do docker-compose.yml, sem perder o volume nem o modo "só base de dados".
- Credenciais só no .env; healthchecks e arranque ordenado; nada de serviços que a arquitetura não tem.

# Task
Serviços, rede, volume, .env.example, healthchecks; validar com os comandos da ficha e no browser.
```

## Alterações

- `docker-compose.yml` renomeado para `compose.yaml` (rename no Git) com três serviços na rede `matriz`:
  - `postgres`: igual ao anterior (porta 5433 no host, volume `matriz-dados`, `pg_isready`);
  - `backend`: imagem `backend/Dockerfile`, liga-se a `postgres:5432`, só arranca com a base de dados saudável, healthcheck em `/actuator/health`;
  - `frontend`: imagem `frontend/Dockerfile` com `VITE_API_URL` como build arg, porta 8000 no host, só arranca com o backend saudável.
- `.env.example`: `MATRIZ_CORS_ORIGENS` passa a incluir `http://localhost:8000`; novas `MATRIZ_FRONTEND_PORTA` e `VITE_API_URL`.
- O modo de desenvolvimento passa a ser `docker compose up -d postgres` (README, AGENTS.md e mensagem do `e2e/preparar-bd.mjs`). O nome do projeto Compose continua a vir da pasta, por isso o volume existente (`matriz-api_matriz-dados`) é o mesmo.

## Validação (2026-11-12, no ambiente cloud)

| Comando / verificação | Resultado |
|-----------------------|-----------|
| `docker compose config -q` | sem erros |
| `docker compose build` | imagens construídas (no cloud com `docker build` e o certificado do proxy, ver nota) |
| `docker compose up -d` | os três serviços arrancam pela ordem certa |
| `docker compose ps` | `matriz-postgres`, `matriz-backend` e `matriz-frontend` `healthy` |
| Browser (Playwright) em http://localhost:8000 | login da Ana, abrir "Website institucional", criar uma tarefa, recarregar: a tarefa aparece; 0 erros na consola |
| `docker compose down` + `up -d` | o volume mantém-se; a tarefa criada continua lá; o seed não se repete ("a base de dados já tem 3 utilizador(es)") |
| `docker compose logs --tail=50` | sem `WARN`, `ERROR` nem `FATAL` |

## Revisão humana e problemas encontrados

- No cloud o `docker compose build` não consegue usar o certificado do proxy (o Compose pede privilégios extra ao BuildKit). As imagens foram construídas com `docker build` com o mesmo contexto e marcadas como `matriz-backend:local` e `matriz-frontend:local`; depois `docker compose up -d --no-build`. O `docker compose build` normal fica validado na CI (Ex. 4) e no Mac.
- A primeira versão tinha `MATRIZ_JWT_SEGREDO: ${MATRIZ_JWT_SEGREDO:?}`; foi retirado o `:?` porque impedia `docker compose up -d postgres` sem segredo. O backend continua a recusar arrancar sem ele.
- O backend em Docker e o `./mvnw spring-boot:run` usam a mesma porta 8080: não se correm os dois ao mesmo tempo.

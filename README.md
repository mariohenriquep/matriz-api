# Matriz

Aplicação de gestão de projetos e tarefas para equipas de trabalho, desenvolvida no âmbito da UC *AI-Powered Software Engineering*.

## O problema

As equipas precisam de um sítio único onde organizar os seus projetos, distribuir tarefas e saber em que ponto está o trabalho. Sem isso, as tarefas perdem-se em emails e conversas, não é claro quem faz o quê, e é difícil perceber quanto falta para terminar um projeto.

Descrição do cliente:
> "Precisamos de uma aplicação onde as nossas equipas possam organizar projetos, gerir tarefas e acompanhar o progresso do trabalho."

## Objetivos

- Organizar o trabalho em projetos com membros.
- Criar, atribuir e atualizar tarefas.
- Acompanhar o progresso de cada projeto.
- Garantir que cada pessoa só vê os projetos que lhe dizem respeito *(pressuposto P-14, por confirmar)*.

## Funcionalidades previstas para o MVP

- Registo e início de sessão; o primeiro administrador global é criado na instalação.
- Criação de projetos por qualquer utilizador, que fica Gestor do projeto.
- Gestão de membros de cada projeto.
- Tarefas com título, descrição, prioridade, responsável e data limite; as que passam do prazo aparecem como "Atrasada".
- Três estados: `Por fazer`, `Em curso`, `Concluída`.
- Lista de tarefas com filtros e quadro Kanban.
- Painel de progresso por projeto.
- Notificações dentro da aplicação.
- "As minhas tarefas".
- Arquivo e reativação de projetos.
- Administração global: promover e despromover administradores, com registo dessas ações.

Detalhe em [`docs/requirements.md`](docs/requirements.md) e [`docs/user-stories.md`](docs/user-stories.md).

## Organização da documentação

| Ficheiro | Conteúdo |
|----------|----------|
| [`README.md`](README.md) | Apresentação do projeto (este ficheiro). |
| [`AGENTS.md`](AGENTS.md) | Instruções para agentes de IA que trabalhem neste repositório. |
| [`docs/requirements.md`](docs/requirements.md) | Visão, utilizadores, requisitos funcionais e não funcionais, âmbito do MVP. |
| [`docs/user-stories.md`](docs/user-stories.md) | Backlog do MVP: user stories, prioridades, critérios de aceitação e casos-limite. |
| [`docs/constraints.md`](docs/constraints.md) | Restrições, pressupostos, segurança e privacidade, decisões técnicas pendentes. |
| [`docs/architecture.md`](docs/architecture.md) | Arquitetura (monólito modular), componentes e decisões transversais. |
| [`docs/database.md`](docs/database.md) | Modelo de dados, diagrama ER (Mermaid) e regras de negócio. |
| [`openapi.yaml`](openapi.yaml) | Contrato REST (OpenAPI 3.1) de todas as operações do MVP. |
| [`docs/implementation-plan.md`](docs/implementation-plan.md) | Plano de implementação da Ficha 03: stack, regras de acesso e incrementos. |
| [`docs/ui/`](docs/ui/) | Wireframe do painel do projeto (`wireframe.png`, fonte em `wireframe.html`). |
| `backend/` | API em Java 21 + Spring Boot 3 (Maven), migrations Flyway e seed. |
| `frontend/` | Interface em React + Vite + TypeScript. |
| [`docs/ai-log/`](docs/ai-log/) | Registo do uso de IA em cada exercício (prompts, análise e revisão humana). |

## Como executar

### Requisitos

- **Java 21** (o Maven não é preciso: o backend traz o `mvnw`, que o descarrega).
- **Node.js 22** e npm.
- **Docker** com Docker Compose, para a base de dados PostgreSQL 16. Em alternativa, um PostgreSQL 16 instalado localmente.

### 1. Configuração

```bash
cp .env.example .env
```

Altera pelo menos `POSTGRES_PASSWORD` no `.env`. O `.env` não entra no Git; o `.env.example` só tem valores fictícios. O backend lê o `.env` da raiz do repositório.

### 2. Base de dados

```bash
docker compose up -d          # PostgreSQL em localhost:5433 (porta em POSTGRES_PORTA)
docker compose ps             # deve mostrar "healthy"
```

A base de dados usa a porta **5433** do computador local, para não colidir com outro PostgreSQL na 5432. Se a 5433 também estiver ocupada, muda a porta no `.env`, em `POSTGRES_PORTA` e em `MATRIZ_DB_URL`.

O esquema é criado pelo **Flyway** quando o backend arranca (scripts em `backend/src/main/resources/db/migration`). Não há passo manual.

Para apagar todos os dados e recomeçar: `docker compose down -v` (apaga o volume da base de dados).

### 3. Backend

```bash
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

A API fica em `http://localhost:8080/api/v1`. Para confirmar que arrancou e que a base de dados responde:

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP","components":{"db":{"status":"UP"},...}}
```

### 4. Dados de demonstração

No primeiro arranque, se a base de dados não tiver utilizadores, o backend cria três contas fictícias e três projetos. Nos arranques seguintes não cria nem apaga nada. A password das três contas é o valor de `MATRIZ_SEED_PASSWORD` no `.env`.

| Conta | Gestor de | Membro de | Sem acesso a |
|-------|-----------|-----------|--------------|
| `ana@matriz.local` | Website institucional | Formação interna | App móvel |
| `rui@matriz.local` | App móvel | Website institucional | Formação interna |
| `joana@matriz.local` | Formação interna | App móvel | Website institucional |

### 5. Frontend

```bash
cd frontend
npm install
npm run dev                   # http://localhost:5173 (os pedidos a /api seguem para o backend)
```

### Testes e verificações

```bash
cd backend && ./mvnw test     # precisa do Docker: os testes usam um PostgreSQL temporário (Testcontainers)
cd frontend && npm run build && npm run lint
```

## Estado atual

- ✅ **Fase 1 — Especificação (Ficha PL 01):** requisitos, backlog e restrições.
- ✅ **Fase 2 — Conceção (Ficha PL 02):** arquitetura, modelo de dados, contrato da API e wireframe.
- 🚧 **Fase 3 — Desenvolvimento (Ficha PL 03):** stack escolhida e estrutura criada (backend, base de dados, migrations, seed e frontend inicial). Seguem-se autenticação, projetos, tarefas, responsáveis, comentários e interface. Progresso em [`docs/implementation-plan.md`](docs/implementation-plan.md).

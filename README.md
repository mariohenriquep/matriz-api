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
- Garantir que cada pessoa só vê os projetos que lhe dizem respeito *(pressuposto P-14)*.

## Funcionalidades implementadas (versão 0.5.0)

- **Acesso:** início e fim de sessão com email e password; o login fica bloqueado 15 minutos depois de 5 tentativas falhadas seguidas; contas de demonstração criadas por seed (desligado por omissão).
- **Projetos:** criar (quem cria fica Gestor), pesquisar pelo nome, editar e eliminar (só o Gestor, com confirmação; apaga também as tarefas, atribuições e comentários).
- **Membros:** o Gestor adiciona membros pelo email e remove-os.
- **Tarefas:** título, descrição, prioridade, data limite e estado (`Por fazer`, `Em curso`, `Concluída`); as que passam do prazo aparecem como "Atrasada". Qualquer membro cria e edita; só quem criou a tarefa ou o Gestor a elimina.
- **Filtros** por estado, prioridade e responsável, guardados no endereço da página, e contagens e progresso do projeto sempre atualizados.
- **Vários responsáveis** por tarefa, sem duplicados, escolhidos entre os membros.
- **Comentários** com autor e data definidos pelo servidor; não se editam nem se apagam.
- **Painel inicial** com os projetos acessíveis e o total de tarefas por estado.
- Cada utilizador só vê e altera os projetos de que é membro, também em pedidos diretos à API.

Ficaram para incrementos seguintes, como extras do MVP original: registo público, administração global, quadro Kanban, notificações, "As minhas tarefas" e arquivo de projetos. Detalhe em [`docs/requirements.md`](docs/requirements.md), [`docs/user-stories.md`](docs/user-stories.md) e [`docs/implementation-plan.md`](docs/implementation-plan.md).

## Tecnologias

| Componente | Tecnologia |
|------------|-----------|
| Backend | Java 21, Spring Boot 3.5 (Web, Security com JWT, Data JPA, Validation, Actuator), Flyway, Maven Wrapper |
| Base de dados | PostgreSQL 16 |
| Frontend | React 19, React Router, TypeScript, Vite 8 |
| Testes | JUnit 5, Testcontainers, ArchUnit; Vitest e Testing Library; Playwright (E2E) |
| Execução | Docker (imagens multi-stage; frontend servido por nginx sem privilégios) e Docker Compose |
| CI | GitHub Actions, Trivy (dependências, configuração e imagens) e SonarQube Cloud |

## Organização da documentação

| Ficheiro | Conteúdo |
|----------|----------|
| [`README.md`](README.md) | Apresentação do projeto (este ficheiro). |
| [`AGENTS.md`](AGENTS.md) | Instruções para agentes de IA que trabalhem neste repositório. |
| [`docs/requirements.md`](docs/requirements.md) | Visão, utilizadores, requisitos funcionais e não funcionais, âmbito do MVP. |
| [`docs/user-stories.md`](docs/user-stories.md) | Backlog do MVP: user stories, prioridades, critérios de aceitação e casos-limite. |
| [`docs/constraints.md`](docs/constraints.md) | Restrições, pressupostos, segurança e privacidade, decisões técnicas pendentes. |
| [`docs/architecture.md`](docs/architecture.md) | Arquitetura (monólito modular), componentes, decisões transversais e camadas do código (clean architecture). |
| [`docs/database.md`](docs/database.md) | Modelo de dados, diagrama ER (Mermaid) e regras de negócio. |
| [`openapi.yaml`](openapi.yaml) | Contrato REST (OpenAPI 3.1) de todas as operações do MVP. |
| [`docs/implementation-plan.md`](docs/implementation-plan.md) | Plano de implementação da Ficha 03: stack, regras de acesso e incrementos. |
| [`docs/verification.md`](docs/verification.md) | Verificação do MVP: 9 cenários com evidência, testes, contrato, limitações e exemplos de uso da IA. |
| [`docs/test-plan.md`](docs/test-plan.md) | Plano de testes (Ficha 04): cenários de tarefas e permissões a partir dos requisitos, nível de teste e teste associado. |
| [`docs/quality-security-review.md`](docs/quality-security-review.md) | Code Review e Security Review (Ficha 04): IDOR com contas de demonstração, findings com evidência e severidade, análise estática e correções. |
| [`docs/delivery-plan.md`](docs/delivery-plan.md) | Plano de entrega (Ficha 05): componentes, variáveis de ambiente, estratégia de Docker, Compose e CI. |
| [`docs/delivery-validation.md`](docs/delivery-validation.md) | Validação final da entrega (Ficha 05): requisitos cumpridos, resultados, problemas e limitações. |
| [`docs/release-notes/`](docs/release-notes/) | Notas de cada versão (a primeira é a 0.5.0). |
| [`CHANGELOG.md`](CHANGELOG.md) | Registo das alterações de cada versão. |
| `compose.yaml`, `backend/Dockerfile`, `frontend/Dockerfile` | Execução em containers (ver "Executar com Docker Compose"). |
| `.github/workflows/ci.yml` | Pipeline de CI (ver "Integração contínua"). |
| [`docs/ui/`](docs/ui/) | Wireframe do painel do projeto (`wireframe.png`, fonte em `wireframe.html`). |
| `backend/` | API em Java 21 + Spring Boot 3 (Maven), em clean architecture; migrations Flyway e seed. |
| `frontend/` | Interface em React + Vite + TypeScript. |
| `scripts/verificar-contrato.py` | Verifica a API em execução contra o `openapi.yaml`. |
| [`docs/ai-log/`](docs/ai-log/) | Registo do uso de IA em cada exercício (prompts, análise e revisão humana). |

## Arquitetura do código

O backend segue uma **clean architecture** em camadas, cada uma num pacote de `backend/src/main/java/pt/matriz/`:

| Pacote | O que contém |
|--------|--------------|
| `domain` | Entidades e regras do negócio (`Utilizador`, `Projeto`, `Membro`, `Papel`…), sem dependências das outras camadas. |
| `repositories` | Acesso à base de dados; devolve objetos do domínio. |
| `services` | Casos de uso: validações, regras de acesso e transações. |
| `controllers` | Endpoints REST; só tratam de HTTP. |
| `dtos` | Pedidos e respostas da API, iguais aos schemas do `openapi.yaml`. |
| `mappers` | Conversão entre domínio e DTOs. |
| `security`, `exceptions`, `config` | Autenticação JWT, erros em Problem Details, configuração e seed. |

As dependências só apontam para dentro (`controllers → services → repositories → domain`), e o teste `ArquiteturaTest` falha se alguém quebrar esta regra. Detalhe em [`docs/architecture.md`](docs/architecture.md) §6.

## Como executar

### Requisitos

- **Java 21** (o Maven não é preciso: o backend traz o `mvnw`, que o descarrega).
- **Node.js 22** e npm.
- **Docker** com Docker Compose, para a base de dados PostgreSQL 16, para os testes do backend e para correr a aplicação completa em containers. Só com o Docker (sem Java nem Node) já se consegue correr a aplicação: ver "Executar com Docker Compose". Em alternativa à base de dados em Docker, um PostgreSQL 16 instalado localmente (ajusta `MATRIZ_DB_URL`, `POSTGRES_USER` e `POSTGRES_PASSWORD`).
- Opcional, para verificar o contrato: Python 3 com `pyyaml` e `jsonschema`.

Resumo, a partir de uma cópia limpa (cada passo está explicado abaixo):

```bash
cp .env.example .env              # e editar: POSTGRES_PASSWORD e MATRIZ_JWT_SEGREDO
docker compose up -d postgres     # só a base de dados (tudo em Docker: ver "Executar com Docker Compose")
cd backend && ./mvnw spring-boot:run          # num terminal
cd frontend && npm install && npm run dev     # noutro terminal; abrir http://localhost:5173
```

### 1. Configuração

```bash
cp .env.example .env
```

No `.env`:

- altera `POSTGRES_PASSWORD`;
- gera o segredo dos tokens, que é obrigatório: `openssl rand -base64 48` e cola o resultado em `MATRIZ_JWT_SEGREDO`. Sem ele, o backend não arranca.

O `.env` não entra no Git; o `.env.example` só tem valores fictícios. O backend lê o `.env` da raiz do repositório.

### 2. Base de dados

```bash
docker compose up -d postgres # PostgreSQL em localhost:5433 (porta em POSTGRES_PORTA)
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
# {"status":"UP"}   (inclui a ligação à base de dados)
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
npm run dev                   # http://localhost:5173
```

O frontend chama a API em `http://localhost:8080/api/v1`. Para outro endereço, copia `frontend/.env.example` para `frontend/.env` e altera `VITE_API_URL`. O backend só aceita pedidos do browser vindos das origens em `MATRIZ_CORS_ORIGENS` (por defeito `http://localhost:5173`); por isso o Vite usa sempre a porta 5173 e recusa arrancar se estiver ocupada.

### Executar com Docker Compose

Em alternativa aos passos 2, 3 e 5, a aplicação completa (PostgreSQL, backend e frontend) corre em containers. Só precisa do Docker e do `.env` do passo 1.

```bash
docker compose up -d --build   # constrói as imagens e arranca os três serviços
docker compose ps              # os três devem ficar "healthy"
```

Abre http://localhost:8000 (porta em `MATRIZ_FRONTEND_PORTA`); a API fica em http://localhost:8080. O backend só arranca depois de a base de dados estar saudável, e o frontend depois do backend. Os dados ficam no volume `matriz-dados` e mantêm-se com `docker compose down`; `docker compose down -v` apaga-os. O frontend chama a API no endereço `VITE_API_URL`, fixado na build da imagem, e a origem `http://localhost:8000` tem de estar em `MATRIZ_CORS_ORIGENS`.

| Para… | Comando |
|-------|---------|
| Validar o `compose.yaml` e o `.env` | `docker compose config -q` |
| Ver o estado e os healthchecks | `docker compose ps` |
| Ver os logs | `docker compose logs --tail=50` (ou `docker compose logs -f backend`) |
| Reconstruir depois de mudar código | `docker compose up -d --build` |
| Parar (mantém os dados) | `docker compose down` |

Problemas comuns:

- **Porta ocupada** (`port is already allocated` ou `address already in use`): muda `POSTGRES_PORTA`, `MATRIZ_PORTA` ou `MATRIZ_FRONTEND_PORTA` no `.env`. Não corras o backend com `./mvnw spring-boot:run` e em Docker ao mesmo tempo: os dois usam a 8080.
- **O backend não fica saudável** (fica em `Restarting`): confirma em `docker compose logs backend` que `MATRIZ_JWT_SEGREDO` tem pelo menos 32 caracteres e que `POSTGRES_PASSWORD` é a mesma com que o volume foi criado (se mudaste a password, `docker compose down -v` recria a base de dados e apaga os dados).
- **O login no frontend falha com erro de rede:** falta `http://localhost:8000` em `MATRIZ_CORS_ORIGENS`, ou o `VITE_API_URL` da imagem não corresponde ao endereço da API (reconstrói com `--build`).

### Sessão e autenticação

1. O login (`POST /api/v1/auth/login`) devolve um token JWT, válido durante **8 horas a partir do login**, com ou sem atividade.
2. O frontend guarda o token no `sessionStorage`: a sessão sobrevive a recarregar a página e termina quando o separador fecha.
3. Todos os pedidos à API, exceto o registo e o login, levam `Authorization: Bearer <token>`. Sem token, ou com um token inválido ou expirado, a API responde `401` e o frontend volta ao login com a mensagem "A tua sessão terminou".
4. "Terminar sessão" (`POST /api/v1/auth/logout`) invalida no servidor todos os tokens desse utilizador, incluindo os de outros separadores.
5. As passwords são guardadas só como hash BCrypt.
6. **Registo público (0.8.0):** "Criar conta" na página inicial (`POST /api/v1/auth/registo`). A conta nova fica no plano gratuito: não cria projetos e só vê os projetos para que um Gestor a adiciona. A resposta é sempre a mesma, exista ou não uma conta com o email. Para dar o plano Profissional a uma conta (ainda sem pagamentos): `UPDATE utilizador SET plano = 'PROFISSIONAL' WHERE email = '...';`.
7. **Limites de pedidos por IP:** 10 logins por minuto e 5 registos por hora (e 50 registos por hora no total). Acima disso a API responde `429`. Configuráveis com `MATRIZ_LIMITE_LOGIN`, `MATRIZ_LIMITE_REGISTO` e `MATRIZ_LIMITE_REGISTO_TOTAL`; atrás de proxies, `MATRIZ_PROXIES_CONFIAVEIS` diz quantos acrescentam o IP ao `X-Forwarded-For` (2 no Render).

Exemplo com `curl`:

```bash
TOKEN=$(curl -s -H 'Content-Type: application/json' \
  -d '{"email":"ana@matriz.local","password":"<MATRIZ_SEED_PASSWORD>"}' \
  http://localhost:8080/api/v1/auth/login | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/auth/eu
curl -X POST -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/auth/logout
```

### Testes e verificações

```bash
cd backend && ./mvnw test     # precisa do Docker: os testes usam um PostgreSQL temporário (Testcontainers);
                              # inclui ArquiteturaTest, que verifica as dependências entre camadas
cd backend && ./mvnw test -Dtest=CampoTextoTest   # só os testes unitários da validação de texto (sem Docker)
cd backend && ./mvnw test -Dtest=CriarEConsultarTarefaTest   # integração: criar e consultar com commit real e limpeza
cd frontend && npm test       # testes de componentes e do cliente da API (Vitest + Testing Library, sem backend)
cd frontend && npm run test:cobertura   # os mesmos testes, com relatório de cobertura em frontend/coverage/
cd e2e && npm ci --ignore-scripts && ./node_modules/.bin/playwright install chromium   # só da primeira vez
cd e2e && npm test            # E2E: arranca backend (8081) e frontend (5174) sobre a BD matriz_e2e, criada de novo;
                              # precisa do PostgreSQL do docker compose ligado (docker compose up -d postgres)
cd frontend && npm run build && npm run lint
```

Com o backend a correr sobre os dados de demonstração, o script de contrato percorre todas as operações do `openapi.yaml` e confirma os códigos e os corpos das respostas:

```bash
pip install pyyaml jsonschema
MATRIZ_SEED_PASSWORD=<valor do .env> python3 scripts/verificar-contrato.py
```

Os resultados da última verificação, com os 9 cenários da Ficha 03, estão em [`docs/verification.md`](docs/verification.md).

### Documentação da API

O contrato da API está em [`openapi.yaml`](openapi.yaml) (OpenAPI 3.1): todas as operações, esquemas, códigos de resposta e erros. Pode ser lido em qualquer visualizador de OpenAPI, por exemplo a extensão OpenAPI do VS Code ou do IntelliJ, ou colando o ficheiro em https://editor.swagger.io. A API não serve uma página Swagger própria, para não acrescentar dependências; o script `scripts/verificar-contrato.py` confirma que a implementação corresponde ao contrato.

### Integração contínua

Cada pull request e cada push para o `main` corre a pipeline [`.github/workflows/ci.yml`](.github/workflows/ci.yml) no GitHub Actions (separador *Actions* do repositório):

| Job | O que verifica |
|-----|----------------|
| Backend | `./mvnw verify`: testes unitários, de arquitetura e de integração |
| Frontend | lint, testes e build |
| E2E | o teste Playwright, com um PostgreSQL do Compose |
| Segurança | Trivy: vulnerabilidades HIGH/CRITICAL com correção nas dependências, configuração e segredos |
| Qualidade | testes do backend e do frontend com cobertura (JaCoCo e Vitest) e SonarQube Cloud (quality gate no PR) |
| Imagens Docker | só depois dos anteriores: build das imagens, Trivy nas imagens e arranque da aplicação com os healthchecks; em tags de versão, publicação no Docker Hub |

Num push de uma tag de versão (`vX.Y.Z`), a mesma pipeline corre de ponta a ponta e, se tudo passar, publica no Docker Hub as imagens que o Trivy analisou: `marrike/matriz-backend` e `marrike/matriz-frontend`, com as tags da versão (por exemplo `0.5.0`) e `latest`. A publicação falha se a tag não corresponder à versão do `pom.xml`, do `openapi.yaml` e dos `package.json`. Precisa da variável `DOCKERHUB_USERNAME` e do secret `DOCKERHUB_TOKEN` nas definições do repositório (*Settings → Secrets and variables → Actions*). A imagem publicada do frontend chama a API em `http://localhost:8080/api/v1`; para outro endereço é preciso construir a imagem com outro `VITE_API_URL`.

A pipeline falha se algum teste falhar, e as imagens só se constroem depois de as verificações passarem. Usa só permissão de leitura do repositório; as credenciais de teste são geradas em cada execução. Exceções aceites do Trivy, com justificação, em [`.trivyignore`](.trivyignore).

### Deploy no Render

A aplicação pode ser publicada no plano gratuito do Render a partir do Blueprint [`render.yaml`](render.yaml): PostgreSQL gratuito, backend a partir da imagem do Docker Hub e frontend como site estático. Passos, limites do plano gratuito (o backend adormece, a base de dados expira ao fim de 30 dias) e atualizações em [`docs/deploy-render.md`](docs/deploy-render.md).

## Estado atual

- ✅ **Fase 1 — Especificação (Ficha PL 01):** requisitos, backlog e restrições.
- ✅ **Fase 2 — Conceção (Ficha PL 02):** arquitetura, modelo de dados, contrato da API e wireframe.
- ✅ **Fase 3 — Desenvolvimento (Ficha PL 03):** MVP full-stack implementado (autenticação, projetos e membros, tarefas com filtros, responsáveis, comentários e interface completa), backend em clean architecture, verificado nos 9 cenários da ficha ([`docs/verification.md`](docs/verification.md)) e revisto para entrega. Progresso em [`docs/implementation-plan.md`](docs/implementation-plan.md).
- ✅ **Fase 4 — Testes e revisão (Ficha PL 04):** plano de testes ([`docs/test-plan.md`](docs/test-plan.md)), testes unitários, de integração com gravação real e E2E com Playwright, e Code Review e Security Review ([`docs/quality-security-review.md`](docs/quality-security-review.md)) com as correções aprovadas, incluindo o bloqueio do login após 5 tentativas falhadas.
- ✅ **Fase 5 — Entrega (Ficha PL 05):** containers Docker, Docker Compose, CI no GitHub Actions com Trivy, SonarQube Cloud e cobertura dos testes, publicação das imagens no Docker Hub ([`docs/delivery-plan.md`](docs/delivery-plan.md), validação em [`docs/delivery-validation.md`](docs/delivery-validation.md)). Versão 0.5.0 ([notas](docs/release-notes/v0.5.0.md)).

- ✅ **Extras:** quadro Kanban e arquivo de projetos (0.6.0), quadro geral (0.7.0), deploy no Render (0.7.1), página inicial pública com registo seguro (0.8.0).

Versão atual: **0.8.0** (histórico em [`CHANGELOG.md`](CHANGELOG.md)).

- ⏭️ **Depois:** os extras do MVP original, começando pelo quadro Kanban e pelo arquivo de projetos.

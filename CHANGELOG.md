# Registo de versões

As versões seguem [Versionamento Semântico](https://semver.org/lang/pt-BR/). Enquanto a major for 0, o contrato pode mudar entre versões menores. Cada versão tem uma tag `vX.Y.Z` no `main`. O mesmo número aparece no contrato (`openapi.yaml`), no backend (`backend/pom.xml`) e no frontend (`frontend/package.json`).

## [0.7.1] - 2026-11-16

Deploy no plano gratuito do Render. Notas da versão em [`docs/release-notes/v0.7.1.md`](docs/release-notes/v0.7.1.md).

### Adicionado
- Blueprint `render.yaml` (PostgreSQL gratuito, backend a partir da imagem do Docker Hub, frontend como site estático) e guia [`docs/deploy-render.md`](docs/deploy-render.md).

### Alterado
- O backend aceita o endereço da base de dados em partes (`MATRIZ_DB_HOST`, `MATRIZ_DB_PORTA`, `POSTGRES_DB`) quando `MATRIZ_DB_URL` não está definida. `MATRIZ_DB_URL` continua a ter prioridade, por isso o `.env` e o Docker Compose não mudam.

## [0.7.0] - 2026-11-16

Quadro geral na página inicial, pedido depois da 0.6.0. Notas da versão em [`docs/release-notes/v0.7.0.md`](docs/release-notes/v0.7.0.md).

### Adicionado
- `GET /tarefas`: tarefas de todos os projetos ativos de que o utilizador é membro, com os filtros `projetoId`, `estado`, `prioridade` e `responsavelId`. Projetos arquivados ficam de fora.
- Separador "Quadro geral" na página inicial (e ligação na barra lateral), com o URL `?vista=quadro`: um Kanban com as tarefas de todos os projetos ativos, sem abrir cada projeto. Cada cartão mostra o projeto (com ligação para ele). Arrastar ou "Mover para" muda o estado e atualiza o resumo do painel.
- Filtros do quadro geral por projeto, por prioridade e "Só as minhas" (as tarefas de que o utilizador é responsável), que cobre a ideia do RF-17 "As minhas tarefas".
- Testes: `QuadroGeralTest` (backend), `Inicio.test.tsx` (frontend) e E2E `quadro-geral.spec.ts`.

## [0.6.0] - 2026-11-16

Extras do MVP depois das fichas (RF-15 e RF-18). Notas da versão em [`docs/release-notes/v0.6.0.md`](docs/release-notes/v0.6.0.md).

### Adicionado
- Quadro Kanban na página do projeto (vista por omissão; a lista continua disponível em "Lista"): uma coluna por estado, arrastar um cartão muda o estado (US-13). Cada cartão tem também um seletor "Mover para", para teclado e ecrãs táteis. A vista fica no URL (`?vista=lista`).
- Arquivo de projetos (US-16): `POST /projetos/{id}/arquivar` e `POST /projetos/{id}/reativar` (só o Gestor), `GET /projetos?arquivados=true`, campos `arquivado` e `arquivadoEm` no `Projeto`. Migração `V4__arquivo_projetos.sql`.
- Separadores "Ativos" e "Arquivados" no painel inicial; aviso e botão "Reativar projeto" num projeto arquivado.
- Novo visual, a partir da proposta aprovada no Figma: barra lateral escura (no telemóvel passa para o topo), cartões arredondados com sombra e efeito ao passar o rato, cartões de resumo por estado no painel, cores por estado e por prioridade, gradiente violeta na marca, modo escuro revisto e tipo de letra Inter servido pela própria aplicação (`@fontsource-variable/inter`, sem pedidos a serviços externos).
- Testes: `ArquivoProjetosTest` (backend), `Quadro.test.tsx` e novos casos no frontend, E2E `quadro-e-arquivo.spec.ts` (arrastar no Chromium, arquivar e reativar).

### Alterado
- Na página do projeto, as tarefas aparecem antes dos membros.
- Um projeto arquivado fica só de leitura: alterar o projeto, membros, tarefas, responsáveis ou comentários dá `409` com o tipo `/erros/projeto-arquivado` (RN-04, CE-05). Consultar e eliminar continuam possíveis.
- A lista de projetos (`GET /projetos`) devolve por omissão só os projetos ativos, e o resumo do painel conta só esses.

## [0.5.0] - 2026-11-16

Fase 5: entrega (Ficha PL 05, PRs #17 a #21). Validação final em [`docs/delivery-validation.md`](docs/delivery-validation.md). Notas da versão em [`docs/release-notes/v0.5.0.md`](docs/release-notes/v0.5.0.md).

### Adicionado
- Plano de entrega em [`docs/delivery-plan.md`](docs/delivery-plan.md).
- Imagens Docker do backend (JRE 21, utilizador sem privilégios) e do frontend (nginx sem privilégios), com `.dockerignore` e healthchecks.
- `compose.yaml` com a aplicação completa (PostgreSQL, backend e frontend), rede própria, volume persistente e arranque ordenado pelos healthchecks.
- Pipeline de CI no GitHub Actions: testes do backend, do frontend e E2E; Trivy no repositório e nas imagens; SonarQube Cloud; build e arranque das imagens só depois das verificações.
- Publicação das imagens no Docker Hub (`marrike/matriz-backend` e `marrike/matriz-frontend`, tags da versão e `latest`) num push de uma tag `vX.Y.Z`, depois de todas as verificações.
- Diagramas Mermaid da execução em containers e da pipeline em [`docs/architecture.md`](docs/architecture.md) §8.
- Cobertura dos testes enviada para o SonarQube Cloud: JaCoCo no backend (`./mvnw verify`) e Vitest no frontend (`npm run test:cobertura`).
- Relatório de validação final em [`docs/delivery-validation.md`](docs/delivery-validation.md).
- README com as tecnologias, a execução com Docker Compose, a documentação da API e a integração contínua.

### Alterado
- `docker-compose.yml` passa a `compose.yaml`. Para desenvolver só com a base de dados em Docker usa-se `docker compose up -d postgres`.
- `.env.example`: `MATRIZ_CORS_ORIGENS` inclui `http://localhost:8000`; novas variáveis `MATRIZ_FRONTEND_PORTA` e `VITE_API_URL`.
- Os testes E2E correm com `npm test` na pasta `e2e/`.
- Versão 0.5.0 em `openapi.yaml`, `backend/pom.xml`, `frontend/package.json` e `e2e/package.json`.

### Segurança
- Tomcat 10.1.61, Jackson 2.21.8 e driver PostgreSQL 42.7.14 fixados no `pom.xml`, por vulnerabilidades HIGH/CRITICAL encontradas pelo Trivy nas versões do Spring Boot 3.5.16.
- `.trivyignore` com 2 vulnerabilidades do `spring-webmvc` sem correção na linha 6.2, em partes que a Matriz não usa (XsltView e Server-Sent Events).
- `npm ci --ignore-scripts` na CI e na imagem do frontend.

## [0.4.0] - 2026-11-12

Fim da Fase 4: testes e revisão (Ficha PL 04, PRs #12 a #15).

### Adicionado
- Plano de testes de tarefas e permissões em [`docs/test-plan.md`](docs/test-plan.md).
- Testes unitários da validação dos campos de texto (`CampoTextoTest`, sem Spring nem BD).
- Teste de integração que cria e consulta uma tarefa com gravação real na base de dados (`CriarEConsultarTarefaTest`).
- Teste E2E com Playwright em `e2e/`, com uma base de dados própria (`matriz_e2e`).
- Code Review e Security Review em [`docs/quality-security-review.md`](docs/quality-security-review.md) e o script `scripts/verificar-idor.sh`.
- Bloqueio do login durante 15 minutos depois de 5 tentativas falhadas seguidas (migration `V3__bloqueio_login.sql`). O contrato descreve este bloqueio.

### Alterado
- Os dados de demonstração (seed) ficam desligados por omissão. Para os ligar, usa-se `MATRIZ_SEED_ATIVO=true` no `.env`.
- Versões alinhadas em 0.4.0 no contrato, no backend, no frontend e no `e2e`.

### Corrigido
- Os limites de caracteres contam caracteres reais (code points): um emoji conta como 1, tanto no backend como no frontend.
- A pesquisa de projetos limpa o texto antes de verificar o comprimento.
- O filtro `responsavelId` lido do URL só aceita valores numéricos.

## [0.3.0] - 2026-11-05

Fim da Fase 3: desenvolvimento do MVP full-stack (Ficha PL 03, PRs #3 a #11).

- Autenticação JWT (8 h) com logout que revoga o token.
- Projetos e membros com papéis.
- Tarefas com estados, prioridades, responsáveis, filtros e pesquisa.
- Comentários.
- Interface React completa.
- Backend em clean architecture.
- Verificado nos 9 cenários de [`docs/verification.md`](docs/verification.md).

Nota: na altura não se criou nenhuma tag. O contrato chegou a estar em 0.3.1 durante a Ficha 04 (a descrição do bloqueio do login) e foi publicado como 0.4.0.

## [0.2.x] - 2026-10-28

Fases 1 e 2: especificação e conceção (Fichas PL 01 e 02, PRs #1 e #2). Esta fase deixou os requisitos, o backlog, a arquitetura, o modelo de dados, o contrato inicial da API (0.2.4) e o wireframe. Só tem documentação.

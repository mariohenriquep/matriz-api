# Registo de versões

As versões seguem [Versionamento Semântico](https://semver.org/lang/pt-BR/). Enquanto a major for 0, o contrato pode mudar entre versões menores. Cada versão tem uma tag `vX.Y.Z` no `main`. O mesmo número aparece no contrato (`openapi.yaml`), no backend (`backend/pom.xml`) e no frontend (`frontend/package.json`).

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

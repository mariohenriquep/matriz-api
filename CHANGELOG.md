# Registo de versões

As versões seguem [Versionamento Semântico](https://semver.org/lang/pt-BR/). Enquanto a major for 0, o contrato pode mudar entre versões menores. Cada versão tem uma tag `vX.Y.Z` no `main`. O mesmo número aparece no contrato (`openapi.yaml`), no backend (`backend/pom.xml`) e no frontend (`frontend/package.json`).

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

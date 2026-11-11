# Plano de testes da Matriz

> Ficha PL 04, Exercício 1. Plano feito a 2026-11-09 sobre o `main` 88f81dd (MVP da Ficha 03). Os cenários partem dos **requisitos** (`docs/requirements.md`) e dos **critérios de aceitação** (`docs/user-stories.md`), não do comportamento atual do código. Os resultados das execuções ficam registados nos exercícios seguintes (§6).

## 1. Âmbito

- **Funcionalidade em teste:** tarefas e as respetivas permissões (RF-08 a RF-12, RNF-03, RNF-06; US-06 a US-10; casos-limite CE-06 e CE-07).
- **Fora do âmbito deste plano:** projetos, membros e comentários (já cobertos pelos testes da Ficha 03, ver `docs/verification.md`), e tudo o que ainda não está implementado: administrador global (RF-02, RF-13, RF-19), Kanban (RF-15), notificações (RF-16), "As minhas tarefas" (RF-17), arquivo (RF-18) e bloqueio após tentativas falhadas (RNF-11). Os critérios de aceitação que dependem destas funcionalidades (ex.: US-07 CA3 com administrador, US-08 CA1 "recebe uma notificação") só são testados na parte já implementada.

## 2. Níveis de teste e ferramentas

| Nível | O que prova | Ferramenta (já no projeto, salvo indicação) | Dados |
|-------|-------------|---------------------------------------------|-------|
| **Unitário** | Uma regra isolada, sem Spring, base de dados nem rede | JUnit 5 + AssertJ (backend); Vitest (frontend) | Valores construídos no próprio teste |
| **Integração** | API + regras + persistência real, pelo contrato HTTP | Spring Boot Test + MockMvc + PostgreSQL 16 em Testcontainers | Seed de demonstração numa base de dados temporária; o teste do Ex. 3 grava a sério e limpa no fim (§5, decisão D-3) |
| **E2E** | Um percurso do utilizador na interface real | **Playwright** (`@playwright/test`, a acrescentar no Ex. 4, pasta `e2e/`) | Base de dados própria `matriz_e2e`, criada de novo em cada execução, contas do seed |

Contas de demonstração (seed): **Ana** (Gestora do *Website institucional*), **Rui** (Membro do *Website*, Gestor da *App móvel*), **Joana** (sem acesso ao *Website*).

## 3. Matriz de cenários: tarefas e permissões

Legenda da coluna **Teste**: nome do teste que já existe (Ficha 03) ou o exercício da Ficha 04 em que vai ser criado. **★** marca os cenários essenciais pedidos pela ficha (§4).

| ID | Requisito / CA | Cenário e entrada | Resultado esperado | Nível | Teste |
|----|----------------|-------------------|--------------------|-------|-------|
| T-01 ★ | RF-08, US-06 CA3, RNF-06 | Criar tarefa sem título, com título vazio ou só com espaços (incluindo espaços não separáveis) | Recusada; `400` com erro no campo `titulo` ("O título é obrigatório"); nada fica gravado | Unitário + Integração | `CampoTextoTest` (Ex. 2); `TarefasTest.validacaoDoTituloEDaDescricao`; `CriarEConsultarTarefaTest.tituloInvalidoDa400ENadaFicaGravado` (Ex. 3) |
| T-02 ★ | RF-08, `constraints.md` (1 a 200) | Título com 1 e com 200 caracteres; título com 201 caracteres | 1 e 200 aceites; 201 recusado com "O título tem no máximo 200 caracteres" | Unitário + Integração | Ex. 2; `TarefasTest.validacaoDoTituloEDaDescricao` |
| T-03 | RF-08 | Título com espaços nas pontas, ex. `"  Rever  "` | Guardado sem os espaços: `"Rever"` | Unitário | Ex. 2 |
| T-04 | RF-08 (limite em **caracteres**) | Título com 200 caracteres fora do plano básico do Unicode (ex.: 200 emojis) | Aceite: são 200 caracteres; 201 recusado. **Defeito confirmado e corrigido no Ex. 2**, ver §5 P-6 e §6 | Unitário + Integração + componente | `CampoTextoTest.ValoresLimite`; `TarefasTest.validacaoDoTituloEDaDescricao`; `FormularioTarefa.test.tsx` |
| T-05 ★ | RF-08, US-06 CA1 | Rui (membro) cria "Rever logótipo" só com o título e depois consulta-a por `GET /tarefas/{id}` e na lista do projeto | `201` com `Location`; estado `POR_FAZER`, prioridade `MEDIA`, sem responsável, criador Rui; o `GET` devolve os mesmos campos; a tarefa está **gravada na base de dados** e aparece na lista | Integração + E2E | `TarefasTest.membroCriaTarefaQueFicaPorFazerComEleComoCriador`; `CriarEConsultarTarefaTest.membroCriaUmaTarefaQueFicaGravadaEPodeSerConsultada` (Ex. 3, com commit real); **Ex. 4** (pela interface) |
| T-06 | US-06 CA2 | Criar com descrição, prioridade `ALTA`, responsável membro e data limite | Todos os campos guardados com esses valores | Integração | `TarefasTest.membroCriaTarefaQueFicaPorFazerComEleComoCriador` (parcial: sem descrição) |
| T-07 | RF-08, CE-06 | Data limite no passado, tarefa não concluída | Aceite e marcada `atrasada: true` | Integração | `TarefasTest.listaAsTarefasDoProjetoComResponsaveisComentariosEAtrasada` |
| T-08 | RNF-06 | Prioridade `URGENTE`, data `31/12/2030`, campos que o cliente não pode enviar (`estado`, `criadorId`) | `400` com o campo em causa | Integração | `TarefasTest.validacaoDoTituloEDaDescricao`, `oClienteNaoPodeIndicarOCriadorNemOEstado` |
| T-09 ★ | US-06 CA4, CE-07, RNF-03 | Joana (não membro) tenta criar, ver, editar e eliminar tarefas do *Website*, trocando ids no URL | `404` "não encontrado" em todas, igual a um id inexistente (não revela que existe); nada é alterado | Integração | `TarefasTest.quemNaoEMembroRecebe404EmTodasAsOperacoes`; `ControloAcessoTest`; `CriarEConsultarTarefaTest.tarefaInexistenteEAlheiaDaoOMesmo404` (Ex. 3); revisto no Ex. 5 (IDOR) |
| T-10 | RF-09, US-07 CA2–CA4 | Rui (membro) elimina uma tarefa que não criou; elimina uma que criou; Ana (Gestora) elimina uma criada pelo Rui | `403` "Só quem criou a tarefa ou o gestor do projeto a pode eliminar"; `204`; `204` | Integração | `TarefasTest.soOCriadorOuOGestorEliminam` |
| T-11 | RF-09, US-07 CA1 | Edição parcial (só alguns campos); edição inválida (título vazio) | `200` com os campos alterados e os outros intactos; `400` e a tarefa fica como estava na base de dados | Integração | `TarefasTest.edicaoParcialEValoresANullApagam`, `edicaoInvalidaDa400ENaoAltera` |
| T-12 | RF-10, US-08 CA2 e CA5 | Atribuir um não membro; atribuir o mesmo responsável duas vezes | `422` (`/erros/responsavel-nao-membro`), igual para um id inexistente; `409` e sem duplicado | Integração | `ResponsaveisEComentariosTest.responsavelTemDeExistirESerMembro`, `aMesmaAtribuicaoNaoFicaDuplicada` |
| T-13 | RF-11, US-09 CA1–CA3 | Mudar o estado em qualquer sentido; estado fora dos três (ex.: `FEITA`, US-09 CA3 dá `Bloqueada`) | Aceite e contagens recalculadas; `400` e estado mantido | Integração | `TarefasTest.qualquerMembroMudaEstadoEPrioridadeEAsContagensAcompanham`, `edicaoInvalidaDa400ENaoAltera` |
| T-14 | RF-12, US-10 | Filtros por estado, prioridade e responsável, combinados; filtro inválido | Só as tarefas que cumprem todos; `400` | Integração | `TarefasTest.filtrosPorEstadoPrioridadeEResponsavelCombinam`, `filtroComValorInvalidoDa400` |
| T-15 | RNF-02 | Pedido sem token ou com token inválido | `401` | Integração | `AutenticacaoTest` |
| T-16 | RF-08, RNF-06 (interface) | No formulário: título vazio; título com mais de 200 caracteres | Mensagem no campo, nenhum pedido enviado | Unitário (componente) | `FormularioTarefa.test.tsx` |
| T-17 ★ | US-06 CA1 (interface) | Ana entra → abre o *Website institucional* → cria uma tarefa → vê-a na lista | A tarefa aparece na lista, sem recarregar, com o título escrito e prioridade Média; continua lá depois de recarregar | E2E | `e2e/tests/criar-tarefa.spec.ts` (Ex. 4) |

## 4. Cenários essenciais escolhidos

A ficha pede três; cobrimo-los em mais de um nível, porque cada nível prova uma coisa diferente.

| Cenário da ficha | Casos | Porquê |
|------------------|-------|--------|
| Título inválido | T-01, T-02 (unitário no Ex. 2, integração no Ex. 3) | É a validação mais usada; o unitário testa a regra e os limites depressa, o de integração prova que a API responde `400` e não grava nada. |
| Criação e consulta de tarefa | T-05 (integração no Ex. 3), T-17 (E2E no Ex. 4) | É o fluxo principal (OBJ-2). O teste de integração confirma a persistência real; o E2E confirma que o utilizador vê o resultado. |
| Acesso sem autorização | T-09 (integração, revisto no Ex. 5) e T-10 | É o risco de segurança principal (IDOR). Um não membro não pode distinguir um recurso alheio de um inexistente; um membro sem papel de Gestor recebe `403`. |

## 5. Revisão das sugestões da IA

A primeira matriz foi gerada pela IA (prompt em `docs/ai-log/f04-ex01-plano-testes.md`) e revista contra os documentos e o código. Pressupostos corrigidos e decisões:

| # | Sugestão da IA | Correção / decisão | Fonte |
|---|----------------|--------------------|-------|
| P-1 | Um não membro recebe `403 Forbidden` | **`404`**: um não membro não pode saber que o projeto existe. `403` só para quem é membro mas não tem o papel necessário. | CE-07, `ControloAcesso` |
| P-2 | Estados `TODO`/`IN_PROGRESS`/`DONE` e título de 3 a 150 caracteres (valores do exemplo da Ficha 03) | Estados `POR_FAZER`/`EM_CURSO`/`CONCLUIDA`; título de 1 a 200 caracteres, como no contrato aprovado. | `openapi.yaml`, decisão C-07 da Ficha 03 |
| P-3 | Testar que o administrador elimina tarefas de outros | Fora do âmbito: o administrador global não está implementado. Fica registado em §1. | `docs/implementation-plan.md` |
| P-4 | Testar a notificação ao atribuir (US-08 CA1) | Fora do âmbito: notificações não implementadas. Testa-se só a atribuição. | idem |
| P-5 | O teste de integração existente (`TarefasTest`) já prova a persistência | **Não chega**: corre dentro de uma transação que é desfeita no fim (`@Transactional`), por isso nunca faz commit. O Ex. 3 acrescenta um teste que grava a sério, confirma por uma consulta independente à base de dados e limpa no fim (decisão D-3). | `TarefasTest`, ficha 04 Ex. 3 |
| P-6 | Valor-limite de 200 "caracteres" com texto simples (`"x".repeat(200)`) | Acrescentar T-04: o código mede o tamanho com `String.length()`, que conta unidades UTF-16 (um emoji conta 2), enquanto o PostgreSQL (`VARCHAR(200)`) e o requisito contam caracteres. **Confirmado no Ex. 2:** um título com 101 a 200 emojis era recusado pelo backend e pelo formulário, embora o PostgreSQL o aceite. Corrigido com autorização do Mário (2026-11-11). | `CampoTexto.validar`, `V1__esquema_inicial.sql` |
| P-7 | Seletores E2E por `data-testid` | Usar seletores acessíveis (`getByRole`, `getByLabel`) que já existem; só mexer no frontend com OK (decisão D-5). | Ficha 04 Ex. 4 |
| P-8 | Medir a qualidade pela cobertura de código | Não usar a cobertura como critério: o critério é cada cenário estar ligado a um requisito e a um teste. | Ficha 04, orientações |

## 6. Execução e resultados

Só execuções reais, feitas no ambiente cloud (Java 21, Node 22, Docker com PostgreSQL 16).

| Exercício | Comando | Resultado |
|-----------|---------|-----------|
| Ex. 2 Unitários (1.ª execução, 2026-11-09) | `cd backend && ./mvnw test -Dtest=CampoTextoTest` | 22 testes, **21 passam, 1 falha**: `duzentosEmojisSaoDuzentosCaracteres` ("O título tem no máximo 200 caracteres"). Análise em §6.1 |
| Ex. 2 Unitários (depois da correção, 2026-11-11) | `cd backend && ./mvnw test -Dtest=CampoTextoTest` | 22 testes, todos passam (menos de 0,1 s, sem Docker) |
| Ex. 2 Suíte completa do backend | `cd backend && ./mvnw test` | **97 testes, 0 falhas** (75 da Ficha 03 + 22 novos) |
| Ex. 2 Frontend | `cd frontend && npm test`, `npm run build`, `npm run lint` | **31 testes, 0 falhas** (28 + 3 novos); build e lint sem erros. Sem a correção, o teste novo do formulário falha (verificado) |
| Ex. 3 Integração (2026-11-11) | `cd backend && ./mvnw test -Dtest=CriarEConsultarTarefaTest` | 3 testes, 0 falhas, à primeira execução (cerca de 8 s, inclui arrancar o PostgreSQL) |
| Ex. 3 Suíte completa, duas vezes seguidas | `cd backend && ./mvnw test` | **100 testes, 0 falhas**, nas duas execuções |
| Ex. 3 Prova de isolamento | o teste novo seguido de `ProjetosTest` e `TarefasTest` (`-Dsurefire.runOrder=alphabetical`) | 33 testes, 0 falhas. **Sem a limpeza** (`@AfterEach` retirado de propósito, temporariamente): `ProjetosTest` falha 3 testes (ex.: `$.total` esperado 2, obtido 5), o que mostra que a limpeza é necessária e funciona. A limpeza foi reposta antes do commit |
| Ex. 4 E2E (2026-11-11) | `docker compose up -d` e depois `cd e2e && npm install && npx playwright test` | **1 teste, passa** (cerca de 19 s na 1.ª execução, incluindo arrancar o backend e o frontend; 9 s na 2.ª) |
| Ex. 4 Repetição e isolamento | a mesma execução, segunda vez | Passa outra vez. A base de dados `matriz_e2e` só tem a tarefa E2E da última execução (foi recriada) e a base de dados de desenvolvimento `matriz` ficou com 0 tabelas, ou seja, intacta. No fim, as portas 8081 e 5174 ficam livres (o Playwright para os servidores) |

### 6.1 Análise da falha do Ex. 2

A falha era **um defeito da aplicação, não do teste**. Evidência:

- O requisito e o contrato (`maxLength: 200`) falam em caracteres; `CampoTexto.validar` usava `String.length()`, que conta unidades UTF-16. `"📌".repeat(200)` tem 200 caracteres e `length()` 400.
- No PostgreSQL 16, `char_length(repeat('📌',200)::varchar(200))` dá 200 e um `INSERT` com 200 emojis numa coluna `VARCHAR(200)` é aceite; com 201 dá `value too long for type character varying(200)`.
- No frontend, `'📌'.repeat(200).length` dá 400 e `[...'📌'.repeat(200)].length` dá 200: o formulário tinha o mesmo defeito.

Não se alterou o teste nem o limite para ficar verde. Com autorização do Mário, a contagem passou a ser feita em caracteres (code points) em `CampoTexto` (título, nome, descrições, comentário) e numa função `caracteres()` do frontend (validação e contadores dos formulários). Testes de regressão: `CampoTextoTest.duzentosEmojisSaoDuzentosCaracteres` e `duzentosEUmEmojisSaoRecusados`, uma linha nova em `TarefasTest.validacaoDoTituloEDaDescricao` (cria a tarefa com 200 emojis no PostgreSQL real), `texto.test.ts` e um caso em `FormularioTarefa.test.tsx`.

**Limitação conhecida:** emojis compostos (ex.: 👨‍👩‍👧, feito de 5 code points) contam como vários caracteres. É o mesmo critério do PostgreSQL, por isso o backend e a base de dados ficam coerentes.

### 6.2 Como o teste de integração garante dados isolados (Ex. 3)

- **Base de dados de teste:** um PostgreSQL 16 temporário (Testcontainers), criado de novo a cada `./mvnw test`, com o esquema do Flyway e o seed. Nunca toca na base de dados de desenvolvimento.
- **Commit real:** `CriarEConsultarTarefaTest` não usa `@Transactional`, ao contrário dos outros testes da API. A tarefa é gravada com commit e lida por uma consulta SQL feita noutra ligação, que só a vê se o commit aconteceu.
- **Dados próprios:** cada teste cria um projeto com nome único (UUID), junta o Rui como membro e trabalha só dentro dele, sem mexer nas tarefas do seed de que os outros testes dependem.
- **Limpeza verificada:** no `@AfterEach` o projeto é eliminado (a cascata leva as tarefas e os membros) e o teste confirma que não ficou nenhuma tarefa nem membro desse projeto.

### 6.3 Como o teste E2E se mantém reprodutível (Ex. 4)

- **Ambiente próprio:** o Playwright arranca o backend na porta 8081 e o frontend na 5174, ligados a uma base de dados `matriz_e2e` que `e2e/preparar-bd.mjs` apaga e cria de novo antes de cada execução (no PostgreSQL do `docker compose`). O Flyway cria o esquema e o seed cria as contas. Como as portas são outras, os testes podem correr com o ambiente de desenvolvimento ligado, e os teus dados ficam intactos.
- **Dados de teste:** a conta Ana do seed, com uma password só da base de dados de E2E e um segredo JWT gerado em cada execução. O título da tarefa leva a hora, para nunca depender de execuções anteriores.
- **Seletores estáveis:** pelo papel e pelo nome acessível (`getByLabel('Email')`, `getByRole('button', { name: 'Entrar' })`, a região "Tarefas"). Não foi preciso mexer no frontend nem acrescentar `data-testid`.
- **Sem esperas fixas:** as asserções do Playwright (`toBeVisible`, `toHaveCount`) esperam até a condição se cumprir. Não há `waitForTimeout`.
- **Evidências só quando falha:** o trace e a captura de ecrã só são guardados em caso de falha (`e2e/test-results/`, relatório em `npx playwright show-report`). Como o teste passou, não foi preciso analisá-los.


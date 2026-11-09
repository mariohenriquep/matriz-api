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
| T-01 ★ | RF-08, US-06 CA3, RNF-06 | Criar tarefa sem título, com título vazio ou só com espaços (incluindo espaços não separáveis) | Recusada; `400` com erro no campo `titulo` ("O título é obrigatório"); nada fica gravado | Unitário + Integração | Ex. 2 (`CampoTextoTest`); `TarefasTest.validacaoDoTituloEDaDescricao`; Ex. 3 |
| T-02 ★ | RF-08, `constraints.md` (1 a 200) | Título com 1 e com 200 caracteres; título com 201 caracteres | 1 e 200 aceites; 201 recusado com "O título tem no máximo 200 caracteres" | Unitário + Integração | Ex. 2; `TarefasTest.validacaoDoTituloEDaDescricao` |
| T-03 | RF-08 | Título com espaços nas pontas, ex. `"  Rever  "` | Guardado sem os espaços: `"Rever"` | Unitário | Ex. 2 |
| T-04 | RF-08 (limite em **caracteres**) | Título com 200 caracteres fora do plano básico do Unicode (ex.: 200 emojis) | Aceite: são 200 caracteres. **Hipótese a validar no Ex. 2**, ver §5 P-6 | Unitário | Ex. 2 |
| T-05 ★ | RF-08, US-06 CA1 | Rui (membro) cria "Rever logótipo" só com o título e depois consulta-a por `GET /tarefas/{id}` e na lista do projeto | `201` com `Location`; estado `POR_FAZER`, prioridade `MEDIA`, sem responsável, criador Rui; o `GET` devolve os mesmos campos; a tarefa está **gravada na base de dados** e aparece na lista | Integração + E2E | `TarefasTest.membroCriaTarefaQueFicaPorFazerComEleComoCriador`; **Ex. 3** (com commit real); **Ex. 4** (pela interface) |
| T-06 | US-06 CA2 | Criar com descrição, prioridade `ALTA`, responsável membro e data limite | Todos os campos guardados com esses valores | Integração | `TarefasTest.membroCriaTarefaQueFicaPorFazerComEleComoCriador` (parcial: sem descrição) |
| T-07 | RF-08, CE-06 | Data limite no passado, tarefa não concluída | Aceite e marcada `atrasada: true` | Integração | `TarefasTest.listaAsTarefasDoProjetoComResponsaveisComentariosEAtrasada` |
| T-08 | RNF-06 | Prioridade `URGENTE`, data `31/12/2030`, campos que o cliente não pode enviar (`estado`, `criadorId`) | `400` com o campo em causa | Integração | `TarefasTest.validacaoDoTituloEDaDescricao`, `oClienteNaoPodeIndicarOCriadorNemOEstado` |
| T-09 ★ | US-06 CA4, CE-07, RNF-03 | Joana (não membro) tenta criar, ver, editar e eliminar tarefas do *Website*, trocando ids no URL | `404` "não encontrado" em todas, igual a um id inexistente (não revela que existe); nada é alterado | Integração | `TarefasTest.quemNaoEMembroRecebe404EmTodasAsOperacoes`; `ControloAcessoTest`; revisto no Ex. 5 (IDOR) |
| T-10 | RF-09, US-07 CA2–CA4 | Rui (membro) elimina uma tarefa que não criou; elimina uma que criou; Ana (Gestora) elimina uma criada pelo Rui | `403` "Só quem criou a tarefa ou o gestor do projeto a pode eliminar"; `204`; `204` | Integração | `TarefasTest.soOCriadorOuOGestorEliminam` |
| T-11 | RF-09, US-07 CA1 | Edição parcial (só alguns campos); edição inválida (título vazio) | `200` com os campos alterados e os outros intactos; `400` e a tarefa fica como estava na base de dados | Integração | `TarefasTest.edicaoParcialEValoresANullApagam`, `edicaoInvalidaDa400ENaoAltera` |
| T-12 | RF-10, US-08 CA2 e CA5 | Atribuir um não membro; atribuir o mesmo responsável duas vezes | `422` (`/erros/responsavel-nao-membro`), igual para um id inexistente; `409` e sem duplicado | Integração | `ResponsaveisEComentariosTest.responsavelTemDeExistirESerMembro`, `aMesmaAtribuicaoNaoFicaDuplicada` |
| T-13 | RF-11, US-09 CA1–CA3 | Mudar o estado em qualquer sentido; estado fora dos três (ex.: `FEITA`, US-09 CA3 dá `Bloqueada`) | Aceite e contagens recalculadas; `400` e estado mantido | Integração | `TarefasTest.qualquerMembroMudaEstadoEPrioridadeEAsContagensAcompanham`, `edicaoInvalidaDa400ENaoAltera` |
| T-14 | RF-12, US-10 | Filtros por estado, prioridade e responsável, combinados; filtro inválido | Só as tarefas que cumprem todos; `400` | Integração | `TarefasTest.filtrosPorEstadoPrioridadeEResponsavelCombinam`, `filtroComValorInvalidoDa400` |
| T-15 | RNF-02 | Pedido sem token ou com token inválido | `401` | Integração | `AutenticacaoTest` |
| T-16 | RF-08, RNF-06 (interface) | No formulário: título vazio; título com mais de 200 caracteres | Mensagem no campo, nenhum pedido enviado | Unitário (componente) | `FormularioTarefa.test.tsx` |
| T-17 ★ | US-06 CA1 (interface) | Ana entra → abre o *Website institucional* → cria uma tarefa → vê-a na lista | A tarefa aparece na lista, sem recarregar, com o título escrito | E2E | **Ex. 4** |

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
| P-6 | Valor-limite de 200 "caracteres" com texto simples (`"x".repeat(200)`) | Acrescentar T-04: o código mede o tamanho com `String.length()`, que conta unidades UTF-16 (um emoji conta 2), enquanto o PostgreSQL (`VARCHAR(200)`) e o requisito contam caracteres. **Hipótese, não confirmada:** um título com 101 a 200 emojis pode ser recusado indevidamente. Verificar no Ex. 2; se for um defeito, decides se se corrige. | `CampoTexto.validar`, `V1__esquema_inicial.sql` |
| P-7 | Seletores E2E por `data-testid` | Usar seletores acessíveis (`getByRole`, `getByLabel`) que já existem; só mexer no frontend com OK (decisão D-5). | Ficha 04 Ex. 4 |
| P-8 | Medir a qualidade pela cobertura de código | Não usar a cobertura como critério: o critério é cada cenário estar ligado a um requisito e a um teste. | Ficha 04, orientações |

## 6. Execução e resultados

Preenchido em cada exercício, só com execuções reais.

| Exercício | Comando | Resultado |
|-----------|---------|-----------|
| Ex. 2 Unitários | *(por executar)* | |
| Ex. 3 Integração | *(por executar)* | |
| Ex. 4 E2E | *(por executar)* | |

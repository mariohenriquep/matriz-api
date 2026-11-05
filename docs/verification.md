# Verificação do MVP da Matriz

> Ficha PL 03, Exercício 8. Verificação feita a 2026-11-05 sobre o ramo do Ex. 7 (frontend completo), com o backend, o frontend e o PostgreSQL a correr localmente a partir de uma base de dados nova (seed de demonstração).

## 1. Ambiente

| Peça | Versão / configuração |
|------|-----------------------|
| Backend | Java 21, Spring Boot 3.5, `./mvnw spring-boot:run`, porta 8080 |
| Base de dados | PostgreSQL 16 (`docker compose up -d`, porta 5433), esquema por Flyway |
| Frontend | Node 22, Vite (`npm run dev`, porta 5173), React 19 |
| Browser | Chromium (Playwright), a 1280 px e a 390 px de largura |
| Contas | Seed: Ana (Gestora do *Website institucional*), Rui (membro do Website, Gestor da *App móvel*), Joana (sem acesso ao Website) |

## 2. Como distinguimos as verificações

- **Automático:** testes que correm com `./mvnw test` (backend, MockMvc + Testcontainers) e `npm test` (frontend, Vitest + Testing Library). Repetem-se sem intervenção.
- **Manual (API):** pedidos `curl` à aplicação em execução, com o pedido, o código HTTP e o início da resposta registados.
- **Manual (browser):** percursos no browser com a aplicação real. Foram conduzidos por um script Playwright, para ficarem repetíveis e com capturas, mas não fazem parte da suíte de testes.

## 3. Cenários

| Caso | Cenário | Resultado esperado | Resultado observado | Evidência | Tipo | Estado |
|------|---------|--------------------|---------------------|-----------|------|--------|
| 01 | Acesso à aplicação | Credenciais válidas entram; inválidas e pedidos sem autenticação são tratados | Login válido devolve token e abre o painel ("Olá, Ana Silva"). Password errada e email inexistente dão o mesmo `401` "Email ou password incorretos" (sem revelar qual falhou). Campos vazios dão `400` com o erro de cada campo. Sem token ou com token inválido, `401` "É preciso iniciar sessão" / "A sessão expirou". Email com maiúsculas entra | §4.1; `01-login-invalido.png`; `AutenticacaoTest` (14 testes) | Automático + API + browser | ✅ |
| 02 | Gestão de projetos | Criar, pesquisar, editar e eliminar funciona de acordo com as permissões | Ana cria, pesquisa ("verifica", "FORMACAO" sem acento), edita e elimina (com confirmação que avisa quantas tarefas se perdem). Rui, membro, recebe `403` ao editar ou eliminar e não vê esses botões. Joana, sem acesso, recebe `404`. No browser a 390 px: criar, pesquisar, editar e eliminar sem scroll horizontal | §4.2; `02-pesquisa-390px.png`; `ProjetosTest` (17 testes) | Automático + API + browser | ✅ |
| 03 | Persistência de tarefas | A tarefa aparece sem recarregar e mantém-se após reiniciar o backend | Ao criar a tarefa no detalhe do projeto, aparece na lista sem navegação nem recarregamento e o progresso passa de 33 % para 25 %. Depois de parar e voltar a arrancar o backend (processo novo), a tarefa continua lá com o estado (Concluída), a prioridade (Alta) e o comentário | §4.3; `03-tarefa-criada.png`; `TarefasTest.membroCriaTarefaQueFicaPorFazerComEleComoCriador` | Automático + API + browser | ✅ |
| 04 | Validação de dados | Campos obrigatórios vazios ou só com espaços e valores inválidos são rejeitados | Nome, título e comentário vazios ou só com espaços, tabulações ou quebras de linha dão `400` com o campo. Título com 201 caracteres, prioridade `URGENTE`, estado `FEITA`, data `31-12-2026` ou `2026-02-30`, email inválido e campos não permitidos (`estado` ao criar, `autor` no comentário, `projetoId` na edição) dão `400`. **Bug encontrado:** um título só com espaços não separáveis (U+00A0) era aceite e a tarefa ficava sem título visível. Corrigido (§5) | §4.4; `TarefasTest.validacaoDoTituloEDaDescricao`, `ProjetosTest.validacaoDoNomeEDaDescricao`, `ResponsaveisEComentariosTest.validacaoDoConteudo`; `FormularioTarefa.test.tsx` | Automático + API | ✅ depois da correção |
| 05 | Estado e prioridade | A edição atualiza a tarefa, os filtros e as contagens de forma coerente | Pela API: passar a tarefa a Em curso + Alta muda as contagens do projeto de `2/0/0` para `1/1/0` e depois, com Concluída, para `1/0/1`. O filtro `estado=CONCLUIDA&prioridade=ALTA` devolve só essa tarefa. No browser: com o filtro "Por fazer" aparece "A mostrar 2 de 4 tarefas"; depois de marcar a tarefa como Concluída no detalhe, o filtro passa a "1 de 4" e o progresso a 50 % | §4.5; `05-detalhe-estado.png`; `TarefasTest.qualquerMembroMudaEstadoEPrioridadeEAsContagensAcompanham`, `filtrosPorEstadoPrioridadeEResponsavelCombinam` | Automático + API + browser | ✅ |
| 06 | Responsáveis | Associar e remover funciona; a mesma associação não fica duplicada | Associar Rui dá `201`; repetir dá `409` "já é responsável" e a lista continua com um só Rui. Associar Joana (não membro) ou um id inexistente dá `422`. Remover dá `204`; remover outra vez dá `404` | §4.6; `ResponsaveisEComentariosTest` (`aMesmaAtribuicaoNaoFicaDuplicada`, `removerCadaAtribuicao`, `responsavelTemDeExistirESerMembro`) | Automático + API | ✅ |
| 07 | Comentários | O conteúdo, o autor autenticado e a data ficam guardados e visíveis | O comentário do Rui fica com autor "Rui Costa" e data do servidor. Tentar enviar `autor` ou `criadoEm` dá `400`. No browser, o comentário aparece com "Ana Silva · 05/11/2026, 11:55" e continua lá depois de reiniciar o backend | §4.7; `ResponsaveisEComentariosTest.autorEDataVemDoServidor` | Automático + API + browser | ✅ |
| 08 | Autorização na API | Outro utilizador não acede nem altera recursos sem permissão, mesmo mudando os identificadores | Joana, trocando os ids no URL, recebe `404` em todas as operações sobre a tarefa e o projeto da Ana (ver, editar, eliminar, comentar, gerir responsáveis e membros, criar tarefas), a mesma resposta de um id inexistente. Rui, membro, recebe `403` ao eliminar uma tarefa que não criou ou remover o Gestor. No browser, Rui abre `/projetos/3` (sem acesso) e vê "Projeto não encontrado" | §4.8; `TarefasTest.quemNaoEMembroRecebe404EmTodasAsOperacoes`, `soOCriadorOuOGestorEliminam`; `ProjetosTest.projetoAlheioEInexistenteDaoOMesmo404`; `ControloAcessoTest` | Automático + API + browser | ✅ |
| 09 | Eliminação e erros | A eliminação respeita as dependências; erros da API não geram mensagens de sucesso nem perda de dados do formulário | Eliminar o projeto de verificação (1 tarefa, 1 comentário, 1 responsável, 2 membros) dá `204`; a tarefa passa a `404` e a base de dados fica com 0 tarefas, comentários, atribuições e membros órfãos. No browser, eliminar uma tarefa pede confirmação (Cancelar mantém-na) e volta ao projeto com "Tarefa … eliminada.". Com o `PATCH` a devolver `500` (simulado), aparece o erro, nenhuma mensagem de sucesso, o título escrito fica no formulário e o botão volta a ficar ativo | §4.9; `09-erro-mantem-dados.png`, `09-tarefa-eliminada.png`; `ProjetosTest.soOGestorEliminaESemDadosOrfaos`, `EsquemaESeedTest.eliminarProjetoNaoDeixaDadosOrfaos`, `TarefaDetalhe.test.tsx`, `FormularioTarefa.test.tsx` | Automático + API + browser | ✅ |

As capturas estão em [`docs/verificacao/`](verificacao/). Em `09-erro-mantem-dados.png`, a mensagem verde "Comentário publicado." pertence ao comentário enviado antes, na secção de comentários; a edição que falhou só mostra o erro.

## 4. Evidência (excertos)

Pedidos feitos com `curl` contra `http://localhost:8080/api/v1`. Formato: `MÉTODO caminho corpo -> código resposta`. As respostas foram encurtadas.

### 4.1 Acesso

```text
POST /auth/login {"email":"ana@matriz.local","password":"errada"} -> 401 {"type":"/erros/credenciais-invalidas","detail":"Email ou password incorretos"}
POST /auth/login {"email":"ninguem@matriz.local","password":"errada"} -> 401 {"type":"/erros/credenciais-invalidas","detail":"Email ou password incorretos"}
POST /auth/login {"email":"","password":""} -> 400 {"erros":[{"campo":"password",...},{"campo":"email",...}]}
GET /projetos (sem token) -> 401 {"detail":"É preciso iniciar sessão."}
GET /projetos (token "abc.def.ghi") -> 401 {"detail":"A sessão expirou ou já não é válida. Inicia sessão novamente."}
GET /auth/eu (Ana) -> 200 {"id":"1","nome":"Ana Silva","email":"ana@matriz.local"}
```

### 4.2 Projetos

```text
POST /projetos {"nome":"Projeto de verificação","descricao":"Ex. 8"} (Ana) -> 201 {"id":"4",...,"meuPapel":"GESTOR"}
GET /projetos?pesquisa=FORMACAO (Ana) -> 200 {"itens":[{"nome":"Formação interna",...}],"total":1}
PATCH /projetos/4 {"nome":"Projeto de verificação (editado)"} (Ana) -> 200
POST /projetos/4/membros {"email":"rui@matriz.local"} (Ana) -> 201
PATCH /projetos/4 {"nome":"Rui tenta"} (Rui) -> 403 {"detail":"Só o gestor do projeto pode fazer esta operação"}
DELETE /projetos/4 (Rui) -> 403
GET /projetos/4 (Joana) -> 404 {"detail":"Projeto não encontrado"}
GET /projetos/999999 (Ana) -> 404 {"detail":"Projeto não encontrado"}
```

### 4.3 Persistência depois de reiniciar o backend

```text
(backend parado e arrancado de novo: "Started MatrizApplication")
GET /tarefas/7 (Ana) -> 200 {"titulo":"Tarefa com espaços","estado":"CONCLUIDA","prioridade":"ALTA","responsaveis":[{"nome":"Ana Silva"}],...}
GET /tarefas/7/comentarios (Ana) -> 200 {"itens":[{"conteudo":"Comentário de verificação","autor":{"nome":"Rui Costa"},...}],"total":1}
Browser: tarefa "Verificação Ex. 8: tarefa persistente" e o seu comentário presentes; estado = CONCLUIDA, prioridade = ALTA
```

### 4.4 Validação

```text
POST /projetos {"nome":"   "} -> 400 {"erros":[{"campo":"nome","mensagem":"O nome é obrigatório"}]}
POST /projetos/4/tarefas {"titulo":"   "} -> 400 {"erros":[{"campo":"titulo","mensagem":"O título é obrigatório"}]}
POST /projetos/4/tarefas {"titulo":"\t\n"} -> 400 (titulo)
POST /projetos/4/tarefas {"titulo":"x","prioridade":"URGENTE"} -> 400 (prioridade)
POST /projetos/4/tarefas {"titulo":"x","dataLimite":"2026-02-30"} -> 400 (dataLimite)
POST /projetos/4/tarefas {"titulo":"aaa…(201)"} -> 400 "O título tem no máximo 200 caracteres"
POST /projetos/4/tarefas {"titulo":"x","estado":"CONCLUIDA"} -> 400 "Campo não permitido: estado"
POST /projetos/4/tarefas {"titulo":"  Tarefa com espaços  ","descricao":"   "} -> 201 {"titulo":"Tarefa com espaços","descricao":null}
PATCH /tarefas/7 {"titulo":null} -> 400 (titulo)
POST /tarefas/7/comentarios {"conteudo":"   "} -> 400 "O comentário não pode estar vazio"

Antes da correção:  POST /projetos/4/tarefas {"titulo":"  "} -> 201 {"titulo":"  ",...}   ← bug
Depois da correção: POST /projetos/4/tarefas {"titulo":"  "} -> 400 "O título é obrigatório"
                    POST /projetos/4/tarefas {"titulo":" Com NBSP "} -> 201 {"titulo":"Com NBSP"}
```

### 4.5 Estado, prioridade, filtros e contagens

```text
GET /projetos/4 -> "contagens":{"POR_FAZER":2,"EM_CURSO":0,"CONCLUIDA":0,"total":2}
PATCH /tarefas/7 {"estado":"EM_CURSO","prioridade":"ALTA"} (Ana) -> 200
GET /projetos/4 -> "contagens":{"POR_FAZER":1,"EM_CURSO":1,"CONCLUIDA":0,"total":2}
PATCH /tarefas/7 {"estado":"CONCLUIDA"} (Rui, membro) -> 200
GET /projetos/4 -> "contagens":{"POR_FAZER":1,"EM_CURSO":0,"CONCLUIDA":1,"total":2}
GET /projetos/4/tarefas?estado=CONCLUIDA&prioridade=ALTA -> total = 1 ["Tarefa com espaços"]
Browser (Website): progresso 33 % → 25 % ao criar → 50 % ao concluir; filtro "Por fazer": "A mostrar 2 de 4" → "A mostrar 1 de 4"
```

### 4.6 Responsáveis

```text
POST /tarefas/7/responsaveis {"utilizadorId":"2"} -> 201 "responsaveis":[{"nome":"Rui Costa"}]
POST /tarefas/7/responsaveis {"utilizadorId":"2"} -> 409 "Este utilizador já é responsável pela tarefa"
POST /tarefas/7/responsaveis {"utilizadorId":"3"} -> 422 "O responsável tem de ser membro do projeto"
POST /tarefas/7/responsaveis {"utilizadorId":"999"} -> 422
DELETE /tarefas/7/responsaveis/2 -> 204
DELETE /tarefas/7/responsaveis/2 -> 404 "Este utilizador não é responsável pela tarefa"
```

### 4.7 Comentários

```text
POST /tarefas/7/comentarios {"conteudo":"Comentário de verificação"} (Rui) -> 201 {"autor":{"id":"2","nome":"Rui Costa"},"criadoEm":"2026-11-05T11:50:58.089833Z"}
POST /tarefas/7/comentarios {"conteudo":"Falsificar","autor":{"id":"1"},"criadoEm":"2000-01-01T00:00:00Z"} -> 400 "Campo não permitido: autor"
Browser: "Ana Silva · 05/11/2026, 11:55 — Comentário pelo browser (Ex. 8)"
```

### 4.8 Autorização com ids trocados (Joana não pertence ao projeto 4)

```text
GET    /tarefas/7                      -> 404 "Tarefa não encontrada"
PATCH  /tarefas/7 {"titulo":"Joana"}   -> 404
DELETE /tarefas/7                      -> 404
GET    /tarefas/7/comentarios          -> 404
POST   /tarefas/7/comentarios          -> 404
POST   /tarefas/7/responsaveis         -> 404
DELETE /tarefas/7/responsaveis/1       -> 404
GET    /projetos/4/tarefas             -> 404 "Projeto não encontrado"
POST   /projetos/4/tarefas             -> 404
POST   /projetos/4/membros             -> 404
DELETE /projetos/4/membros/2           -> 404
Rui (membro): DELETE /projetos/4/membros/1 -> 403; DELETE /tarefas/7 (criada pela Ana) -> 403
Ana: PATCH /tarefas/7 {"projetoId":"3"} -> 400 "Campo não permitido: projetoId"
```

### 4.9 Eliminação

```text
Antes:  projeto 4 com tarefas=1, comentários=1, responsáveis=1, membros=2
DELETE /projetos/4 (Ana) -> 204
GET /projetos/4 -> 404; GET /tarefas/7 -> 404
Depois (linhas órfãs na BD): tarefa=0, comentario=0, tarefa_responsavel=0, membro_projeto=0
Browser: PATCH com 500 simulado → alerta "Erro simulado no Ex. 8"; título no formulário = "Título que não pode perder-se"; botão Guardar ativo
Browser: Eliminar → Cancelar → Eliminar → "Eliminar definitivamente" → projeto com "Tarefa "Verificação Ex. 8: tarefa persistente" eliminada."
```

## 5. Bug encontrado e corrigido

| | |
|--|--|
| **Passos** | `POST /projetos/{id}/tarefas` com `{"titulo":"  "}` (dois espaços não separáveis, como os que aparecem ao copiar texto de uma página web ou de um documento) |
| **Esperado** | `400`, "O título é obrigatório" (cenário 04: só com espaços) |
| **Observado** | `201`; a tarefa ficou com o título `"  "`, invisível na lista. O mesmo acontecia com o nome do projeto, a descrição e os comentários |
| **Causa (confirmada)** | Os serviços validavam com `String.isBlank()` e limpavam com `String.trim()`. Em Java, `trim()` só retira caracteres até U+0020 e `isBlank()` usa `Character.isWhitespace`, que exclui de propósito os espaços não separáveis (U+00A0, U+2007, U+202F). O frontend não deixava passar este caso, porque o `trim()` do JavaScript já os retira; o problema era só da API |
| **Correção** | Nova classe `services/Texto` com `limpo(String)`: retira das pontas o que o `trim()` já retirava, mais os separadores Unicode (`\p{Z}`) e o BOM, e devolve `null` se não sobrar nada. `ServicoTarefas`, `ServicoProjetos` (nome, descrição e pesquisa) e `ServicoComentarios` passaram a usá-la. Sem outras alterações de comportamento |
| **Regressão** | Novos casos em `TarefasTest.validacaoDoTituloEDaDescricao` (U+00A0, U+2007, U+202F e U+3000 recusados; `"  Rever  "` guardado como `"Rever"`), `ProjetosTest.validacaoDoNomeEDaDescricao` e `ResponsaveisEComentariosTest.validacaoDoConteudo`. Antes da correção falhavam os 3 testes (`Status expected:<400> but was:<201>`); depois, os 74 testes passam |

## 6. Testes automáticos

Os três testes pedidos pela ficha:

| Pedido da ficha | Teste | O que verifica |
|-----------------|-------|----------------|
| Uma regra de validação | `TarefasTest.validacaoDoTituloEDaDescricao` | Título ausente, só com espaços (incluindo não separáveis), acima de 200 caracteres, descrição longa, prioridade e data inválidas dão `400` com o campo; 200 caracteres é aceite |
| Um cenário de autorização | `TarefasTest.quemNaoEMembroRecebe404EmTodasAsOperacoes` | Um utilizador fora do projeto recebe `404` ao listar, criar, ver, editar e eliminar tarefas, mesmo com ids válidos |
| Criação com consulta posterior à persistência | `TarefasTest.membroCriaTarefaQueFicaPorFazerComEleComoCriador` | `POST` cria a tarefa; um `GET` posterior, noutro pedido e por outro utilizador, devolve-a da base de dados com os mesmos dados, e as contagens do projeto refletem-na |

Execução completa:

| Comando | Resultado |
|---------|-----------|
| `cd backend && ./mvnw test` | ✅ 74 testes, 0 falhas (`ArquiteturaTest` 6, `EsquemaESeedTest` 6, `ProjetosTest` 17, `ResponsaveisEComentariosTest` 10, `TarefasTest` 13, `AutenticacaoTest` 14, `ControloAcessoTest` 5). Antes da correção: 71 a passar e 3 novas regressões a falhar |
| `cd frontend && npm test` | ✅ 22 testes, 0 falhas (5 ficheiros) |
| `cd frontend && npm run build && npm run lint` | ✅ sem erros nem avisos |

Os testes do backend correm contra um PostgreSQL verdadeiro (Testcontainers), por isso a persistência é verificada na base de dados e não num duplo.

## 7. Limitações e o que não foi verificado

- **Expiração real das 8 horas:** não se esperou 8 horas no browser. Está coberta por `AutenticacaoTest.tokenExpiradoDa401` (token com data passada) e pelo teste do cliente do frontend que termina a sessão num `401`.
- **Browsers:** só Chromium. Firefox e Safari não foram testados.
- **Concorrência:** não se testou dois utilizadores a editar a mesma tarefa ao mesmo tempo (a última escrita ganha; não há controlo de versões).
- **Carga e desempenho:** fora do âmbito; não medido.
- **Mac do Mário:** a verificação correu no ambiente cloud. No Mac, os mesmos comandos devem dar o mesmo resultado (porta 5433, Docker para os testes).
- **Percursos no browser:** foram feitos com scripts Playwright à aplicação real, mas esses scripts não ficaram na suíte automática. Passá-los a testes end-to-end seria um passo seguinte.

## 8. Observações (não são bugs; ficam para decisão)

- Depois de **eliminar um projeto**, o painel não mostra mensagem de sucesso, ao contrário de eliminar uma tarefa, que volta ao projeto com "Tarefa … eliminada.". O projeto desaparece da lista, por isso o resultado é visível, mas o comportamento não é igual.
- Os **filtros da lista de tarefas** não ficam guardados ao abrir uma tarefa e voltar atrás: a lista volta a "Todos". Guardá-los no URL resolveria isto.

# Ficha 03 — Exercício 7: Implementar o frontend da Matriz

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Completar a interface da Matriz (incremento I-6): painel, projetos, detalhe do projeto e detalhe da tarefa.

# Context
docs/ui/wireframe.png, docs/user-stories.md, openapi.yaml 0.2.4, docs/implementation-plan.md (I-6),
frontend existente (Ex. 3 a 6): cliente da API, sessão, Inicio, ProjetoDetalhe, Tarefas, TarefaDetalhe.

# Constraints
- Estados de carregamento, vazio, sucesso e erro em todos os ecrãs.
- Sem submissões repetidas; os dados do formulário ficam quando o pedido falha.
- Sessão expirada (401) volta ao login.
- Eliminar tarefa só para quem a criou ou para o Gestor, com confirmação.
- Kanban e "As minhas tarefas" ficam como extras.

# Task
Propor o plano; depois implementar, testar com Vitest e verificar no browser (computador e 390 px).

# Validation
- npm test, npm run build, npm run lint.
- Percurso completo no browser; alterações continuam lá ao recarregar e depois de reiniciar o backend.
```

## Plano apresentado

Publicado na conversa antes de implementar:

1. Detalhe da tarefa: editar, mudar estado e prioridade, eliminar (criador ou Gestor, com confirmação) e voltar ao projeto.
2. Detalhe do projeto: barra "% concluído" como no wireframe e "a mostrar X de Y tarefas" com filtros ativos.
3. Painel: saudação ao utilizador autenticado e totais de tarefas de todos os projetos.
4. Link "Projetos" na barra de topo e revisão do layout a 390 px.
5. Testes Vitest + Testing Library (dependências novas previstas no plano).

## Resultado

| Ficheiro | Alteração |
|----------|-----------|
| `paginas/TarefaDetalhe.tsx` | Novo bloco `DadosDaTarefa`: seletores de estado e prioridade (guardam só esse campo), Editar com o `FormularioTarefa`, Eliminar com confirmação (só criador ou Gestor); depois de eliminar volta ao projeto com a mensagem |
| `paginas/ProjetoDetalhe.tsx` | Barra de progresso; mostra a mensagem que vem de outra página e limpa-a do histórico para não reaparecer ao recarregar |
| `componentes/Tarefas.tsx` | "A mostrar X de Y tarefas" quando há filtros |
| `componentes/Contagens.tsx` | `Progresso` (`role="progressbar"`, percentagem arredondada, sem divisão por zero) |
| `paginas/Inicio.tsx` | "Olá, \<nome\>", número de projetos e totais por estado; barra de progresso em cada cartão |
| `componentes/Barra.tsx` | Navegação com o link "Projetos"; a barra quebra de linha em ecrãs estreitos |
| `api/tarefas.ts`, `api/projetos.ts` | `alteracoesDe` (só os campos alterados vão no PATCH) e `somarContagens`, fora dos componentes |
| Testes | `vite.config.ts` (jsdom), `src/testes/` (preparação e dados fictícios), 5 ficheiros `*.test.ts(x)`, script `npm test` |
| Docs | README (estado e `npm test`), AGENTS.md (verificações do frontend), plano (I-6 feito) |

Dependências novas (só de desenvolvimento): `vitest`, `jsdom`, `@testing-library/react`, `@testing-library/dom`, `@testing-library/user-event`, `@testing-library/jest-dom`.

## Validação

| Verificação | Resultado |
|-------------|-----------|
| `npm test` | ✅ 22 testes, 0 falhas |
| Validação de formulário | ✅ título vazio ou acima de 200 caracteres não chega à API; o erro fica junto ao campo (`aria-invalid`) |
| Dados mantidos em erro | ✅ com o servidor a falhar, título e descrição ficam preenchidos e o botão volta a estar ativo; erros de campo da API aparecem no campo certo |
| Sem submissões repetidas | ✅ com o pedido em curso o botão fica desativado e um segundo envio do formulário não faz novo pedido |
| Sessão expirada | ✅ um 401 num pedido autenticado limpa a sessão e avisa a aplicação; um 401 no login não |
| Permissões no detalhe | ✅ membro que não criou a tarefa não vê "Eliminar"; eliminar pede confirmação; se falhar, fica na tarefa com o erro |
| `npm run build`, `npm run lint` | ✅ sem erros nem avisos |
| **Percurso no browser (Ana, Gestora)** | ✅ painel com "Olá, Ana Silva", 2 projetos e totais; progresso "33% concluído · 1 de 3 tarefas"; filtro "Por fazer" mostra "A mostrar 1 de 3 tarefas"; criar tarefa atualiza o progresso; no detalhe muda prioridade, estado, título e data; recarregar mantém tudo; título vazio na edição é recusado; eliminar → Cancelar → Eliminar volta ao projeto com a mensagem, que não reaparece ao recarregar |
| Rui (membro) | ✅ vê "Editar" mas não "Eliminar" numa tarefa da Ana; depois de eliminada, o link dá "Tarefa não encontrada" |
| 390 px | ✅ painel, projeto e detalhe sem scroll horizontal; sem erros na consola |
| Persistência | ✅ tarefa criada e alterada (Concluída, Alta) continua igual depois de reiniciar o backend |

## Correções durante a implementação

- O `oxlint` avisou que exportar funções de ficheiros de componentes estraga o recarregamento rápido do Vite. `alteracoesDe` e `somarContagens` passaram para `api/`.
- A 390 px o nome do utilizador e o botão "Terminar sessão" ficavam espremidos em várias linhas. A barra passou a quebrar de linha.
- Num teste, o nome acessível do campo "Título" incluía a mensagem de erro (o erro está dentro do `<label>`). O teste passou a procurar o campo pelo início do nome; o comportamento para leitores de ecrã mantém-se aceitável, porque o erro também está em `aria-describedby`.

## Decisões tomadas neste exercício (a validar)

- **Os totais do painel contam todos os projetos** e não mudam com a pesquisa por nome.
- **Mudar estado ou prioridade no detalhe guarda logo**, sem botão, como na lista do projeto.
- **Kanban e "As minhas tarefas" continuam como extras**, como combinado.

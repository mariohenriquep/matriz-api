# Ficha 03 — Exercício 5: Implementar tarefas

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Tarefas dentro de um projeto (incremento I-4): criar, consultar, editar, eliminar, estado, prioridade e filtros.

# Context
openapi.yaml (/projetos/{id}/tarefas, /tarefas/{id}), US-06, US-07, US-09, US-10, P-04, P-27,
docs/implementation-plan.md §3, ControloAcesso, clean architecture (docs/architecture.md §6).

# Constraints
- Classes nas camadas domain, repositories, services, controllers, dtos e mappers; ArquiteturaTest verde.
- Título 1–200 caracteres e não só espaços; descrição até 2000; valores de estado e prioridade do contrato (C-07).
- Estado inicial POR_FAZER; o criador é o utilizador do token (o cliente não indica criador nem estado).
- Qualquer membro cria e edita; só o criador ou o Gestor eliminam (403 para os outros membros, 404 para não membros).
- Responsáveis na criação: só membros (422), sem repetições (400).
- Filtros por estado, prioridade e responsável, combinados com E lógico.
- "Atrasada" é calculada, não guardada.
- As contagens do projeto atualizam-se na interface sem recarregar a página.

# Task
1. Backend nas camadas, com testes por HTTP.
2. Lista de tarefas no detalhe do projeto: filtros, formulário de criação e edição, mudança rápida de estado,
   eliminação com confirmação.

# Validation
- mvn test; npm run build e lint.
- Percurso no browser com um membro que não é Gestor.
```

## Resultado

| Camada | Ficheiros |
|--------|-----------|
| `domain` | `Tarefa` (com `atrasada(hoje)`), `EstadoTarefa`, `Prioridade`, `FiltroTarefas`, `Pessoa`, `CriacaoTarefa`, `EdicaoTarefa` |
| `repositories` | `TarefaRepositorio`: SQL explícito; responsáveis de todas as tarefas numa só consulta; `alterar` só aceita colunas de uma lista fixa |
| `services` | `ServicoTarefas`: validação, regras de acesso (via `ControloAcesso`), responsáveis membros e sem repetições, eliminar só criador ou Gestor |
| `controllers` | `ControladorTarefas`: 5 operações do contrato; calcula "hoje" no fuso `MATRIZ_FUSO_HORARIO` |
| `dtos` | `NovaTarefa`, `AlteracaoTarefa` (distingue campo ausente de `null`), `TarefaResposta` |
| `mappers` | `TarefaMapper`: DTO ↔ domínio; ids de responsáveis não numéricos dão 400 |
| `exceptions` | `TratamentoErros` passa a indicar o campo quando um valor tem tipo errado (ex.: `estado: "FEITA"`) ou o campo não existe no contrato ("Campo não permitido: criadorId") |
| Testes | `controllers/TarefasTest` (13 testes por HTTP) |
| Frontend | `api/tarefas.ts`, `componentes/Tarefas.tsx` (lista, filtros, estado rápido, editar, eliminar), `componentes/FormularioTarefa.tsx`; `ProjetoDetalhe` volta a pedir as contagens depois de cada alteração |
| Configuração | `MATRIZ_FUSO_HORARIO` (por omissão `Europe/Lisbon`) em `application.yml` e `.env.example` |
| Contrato | `openapi.yaml` 0.2.3: só descrições (fuso de `atrasada`, mensagem do 403 ao eliminar) |

## Validação

| Verificação | Resultado |
|-------------|-----------|
| `mvn test` | ✅ 61 testes, 0 falhas (13 novos de tarefas; `ArquiteturaTest` verde com as classes novas) |
| Listar | ✅ 3 tarefas do Website, com responsáveis por ordem de nome, 2 comentários nas maquetas, "atrasada" só na tarefa com prazo passado e não concluída |
| Filtros | ✅ estado, prioridade, responsável e combinações; valor inválido dá 400 com o campo |
| Acesso | ✅ Joana (não membro) recebe 404 em listar, criar, consultar, editar e eliminar, igual a uma tarefa inexistente; nada muda na base de dados |
| Criar | ✅ 201 com `Location`, `POR_FAZER`, prioridade `MEDIA` por omissão, criador = utilizador do token; contagens do projeto sobem |
| Validação | ✅ título em falta, só espaços ou com 201 caracteres; descrição com 2001; prioridade e data inválidas → 400 com o campo; 200 caracteres aceites |
| Campos proibidos | ✅ `criadorId` ou `estado` na criação dão 400 "Campo não permitido" |
| Responsáveis | ✅ não membro 422 `/erros/responsavel-nao-membro`; repetido ou não numérico 400; nada é criado |
| Editar | ✅ qualquer membro muda estado e prioridade (qualquer transição); `descricao` e `dataLimite` a `null` apagam; pedido vazio, título só espaços, estado inválido ou nulo → 400 sem alterar |
| Eliminar | ✅ membro não criador 403; criador 204; Gestor elimina a tarefa de outro; atribuições e comentários saem em cascata |
| Persistência | ✅ tarefa criada por `curl` continua lá depois de reiniciar o backend |
| **Percurso no browser (Rui, membro)** | ✅ vê 3 tarefas e "Atrasada"; não vê Eliminar na tarefa da Ana; título só com espaços recusado e descrição mantida; cria tarefa (Alta, data, Ana responsável) e "Por fazer" passa de 1 para 2 sem recarregar; filtros Alta → 3, Alta + Rui → 1, + Por fazer → "Nenhuma tarefa corresponde aos filtros"; limpar filtros; muda o estado e "Em curso" passa a 2; edita o título; recarrega e continua lá; eliminar → Cancelar → Eliminar; contagens voltam ao início. Também verificado a 390 px de largura |
| `npm run build`, `npm run lint` | ✅ sem erros nem avisos |

## Correções durante a implementação

- No teste de browser, a mensagem "Tarefa criada" aparecia antes de a lista ser atualizada, e por instantes a tarefa nova ainda não se via. A lista passou a ser pedida primeiro e a mensagem só aparece depois.
- Na primeira versão do serviço, a conversão de datas para `java.sql.Date` estava em `ServicoTarefas`. Passou para `TarefaRepositorio`, porque é um detalhe da base de dados e não uma regra de negócio.

## Decisões tomadas neste exercício (a validar)

- **"Hoje" no fuso da equipa** (`Europe/Lisbon`, configurável): a data limite é um dia sem hora; sem fuso fixo, uma tarefa podia aparecer atrasada uma hora mais cedo ou mais tarde consoante o servidor.
- **Responsáveis só na criação**: a edição de responsáveis (adicionar e remover numa tarefa existente) é do Exercício 6, com `POST/DELETE /tarefas/{id}/responsaveis`.
- **Mudança rápida de estado na lista**, sem abrir o formulário; o formulário de edição também permite mudar o estado.
- **Ordem da lista**: mais recentes primeiro, como no contrato.

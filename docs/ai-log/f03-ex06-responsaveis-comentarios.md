# Ficha 03 — Exercício 6: Implementar responsáveis e comentários

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Vários responsáveis por tarefa e comentários com autor e data (incremento I-5).

# Context
openapi.yaml (/tarefas/{id}/responsaveis, /tarefas/{id}/comentarios), US-08, US-17, RN-02,
docs/implementation-plan.md §3, ControloAcesso, clean architecture (docs/architecture.md §6).

# Constraints
- Elegíveis = membros do projeto; validar existência e elegibilidade no backend.
- A mesma associação tarefa–utilizador não pode repetir-se (409); cada atribuição pode ser removida.
- Comentários com 1–2000 caracteres e não só espaços; autor e data obtidos no backend.
- Comentários não se editam nem se apagam (decisão do Mário).
- Quem não é membro recebe 404 em todas as operações.

# Task
Analisar e propor o plano; depois implementar backend (camadas), testes e o ecrã de detalhe da tarefa.

# Validation
- mvn test; npm run build e lint.
- Atribuições e comentários continuam disponíveis ao reabrir a tarefa e depois de reiniciar o backend.
```

## Plano apresentado

Publicado na conversa antes de implementar: regras de atribuição (409 repetido, 422 não membro ou inexistente, 404 ao remover uma atribuição que não existe), comentários só por membros, e um ecrã novo de detalhe da tarefa com responsáveis e comentários. A edição completa da tarefa nesse ecrã fica para o Ex. 7.

## Resultado

| Camada | Ficheiros |
|--------|-----------|
| `domain` | `Comentario` |
| `repositories` | `ComentarioRepositorio` (data `clock_timestamp()` da base de dados); `TarefaRepositorio.eResponsavel`, `removerResponsavel` |
| `services` | `ServicoTarefas.atribuir`, `removerResponsavel`; `ServicoComentarios` (listar, criar) |
| `controllers` | `ControladorTarefas` (2 rotas novas), `ControladorComentarios` |
| `dtos` | `NovaAtribuicao`, `NovoComentario`, `ComentarioResposta` |
| `mappers` | `ComentarioMapper`; `TarefaMapper.paraResponsavel` (id em falta ou não numérico → 400) |
| Testes | `controllers/ResponsaveisEComentariosTest` (10 testes por HTTP) |
| Frontend | `paginas/TarefaDetalhe.tsx` (rota `/tarefas/:id`): dados, responsáveis (atribuir entre os elegíveis, remover com confirmação), comentários (lista com autor e data, formulário); título da tarefa na lista passa a ser um link |
| Contrato | `openapi.yaml` 0.2.4: só descrições das regras acima |

## Validação

| Verificação | Resultado |
|-------------|-----------|
| `mvn test` | ✅ 71 testes, 0 falhas (10 novos; `ArquiteturaTest` verde) |
| Vários responsáveis | ✅ o Rui (membro) atribui a Ana e depois a si próprio; a tarefa devolve os dois por ordem de nome; o filtro por responsável encontra-a |
| Sem duplicados | ✅ atribuir de novo dá 409 "Este utilizador já é responsável pela tarefa"; continuam 2 linhas |
| Existência e elegibilidade | ✅ Joana (não membro) e id 999999 dão o mesmo 422; id em falta ou "abc" dão 400; campo extra dá 400 |
| Remover | ✅ 204; repetir dá 404; o utilizador continua membro do projeto |
| Comentários | ✅ listados do mais antigo para o mais recente com autor e data; novo comentário com autor do token e data do servidor; `autorId` ou `criadoEm` no pedido dão 400; vazio, só espaços ou com 2001 caracteres dão 400; 2000 aceites; `totalComentarios` acompanha |
| Acesso | ✅ Joana recebe 404 ao atribuir, remover, listar e comentar; nada muda |
| **Percurso no browser (Rui)** | ✅ abre a tarefa pela lista; atribui Ana e Rui (cada um deixa de aparecer como elegível); remover → Cancelar → Remover; comentário só com espaços recusado; comentário publicado aparece no fim com autor e data; recarregar → responsável e comentário continuam lá; a lista do projeto mostra o responsável e o número de comentários; a Joana, com o link da tarefa, vê "Tarefa não encontrada" |
| Persistência | ✅ depois de reiniciar o backend, a tarefa mantém a Ana como responsável e os comentários com autor e data |
| `npm run build`, `npm run lint` | ✅ sem erros nem avisos |

## Correções durante a implementação

- A data dos comentários aparecia como "29/10/26" (ano com 2 dígitos, formato curto do `pt-PT`). Passou a "29/10/2026, 21:24".

## Decisões tomadas neste exercício (a validar)

- **Id inexistente e não membro dão o mesmo 422**, para não revelar que contas existem (como o 404 de CE-07).
- **Qualquer membro atribui e remove responsáveis**, incluindo a si próprio (regras do plano §3).
- **Comentários guardados sem espaços nas pontas**; quebras de linha dentro do texto mantêm-se.
- **Detalhe da tarefa numa página própria** (`/tarefas/:id`), como pede o Ex. 7; editar e eliminar continuam na lista até lá.

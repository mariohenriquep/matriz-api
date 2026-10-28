# Ficha 01 — Exercício 3: User Stories e Acceptance Criteria

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B). Entrada: o âmbito do MVP e as decisões do Exercício 2 (`ex02-analise-requisitos.md`).

## Prompt

```text
# Goal
Converter o âmbito do MVP da Matriz num backlog inicial de User Stories claras,
priorizadas e testáveis.

# Context
Em anexo: análise do Exercício 2 (funcionalidades F-01 a F-14 no MVP, pressupostos
P-01 a P-22, perfis Administrador global, Gestor de projeto e Membro de projeto).
Decisões confirmadas: três estados fixos (Por fazer, Em curso, Concluída) com qualquer
transição; projetos arquivam-se e não se apagam; Kanban e notificações estão no MVP;
qualquer utilizador cria projetos; o administrador global gere todos os projetos e
promove outros a administrador.

# Constraints
- Atua como Product Owner e analista de QA.
- Formato: "Como [tipo de utilizador], quero [funcionalidade], para [benefício]."
- Só funcionalidades do MVP (F-01 a F-14). Não inventes requisitos novos; se faltar
  informação, marca como pressuposto.
- Critérios de aceitação em Given / When / Then, com valores concretos (nomes,
  números, mensagens) e sem palavras vagas como "rápido", "fácil" ou "intuitivo".
- Sem tecnologias nem código. Português de Portugal.

# Task
1. Escreve uma User Story por funcionalidade do MVP, com prioridade MoSCoW e ligação à F-xx.
2. Define critérios de aceitação para todas as Must e Should.
3. Lista casos excecionais e casos-limite, com o comportamento esperado.
4. Revê o resultado e assinala ambiguidades que removeste.

# Validation
- Pelo menos 5 User Stories, critérios em pelo menos 3, pelo menos 3 casos-limite.
- Cada critério pode ser verificado com um "sim/não" num teste.
- Cada User Story liga-se a uma F-xx do Exercício 2.
```

## Resultado

O backlog resultante (16 User Stories, critérios de aceitação e 12 casos-limite) foi guardado em [`docs/user-stories.md`](../user-stories.md), que é o ficheiro pedido no Exercício 4.

## Validação

| Critério | Resultado |
|----------|-----------|
| ≥ 5 User Stories | ✅ 16 (US-01 a US-16) |
| Critérios de aceitação em ≥ 3 | ✅ em todas as 16 |
| ≥ 3 casos-limite | ✅ 12 (CE-01 a CE-12) |
| Ligação a F-xx | ✅ coluna "Funcionalidade" no resumo |
| Sem tecnologia | ✅ |

## Revisão: ambiguidades eliminadas

Na revisão foram procuradas formulações vagas ou requisitos não confirmados. A tabela mostra cada formulação ambígua, o problema e como ficou escrita no backlog.

| Formulação ambígua | Problema | Como ficou |
|--------------------|----------|----------------|
| "A lista carrega rapidamente." | "Rapidamente" não é verificável. | Removido do critério; o desempenho fica como requisito não funcional (Ex. 4). |
| "O utilizador é notificado." | Não dizia onde, nem quando, nem a quem. | Notificação visível no ícone de notificações, com contador de não lidas, para o utilizador afetado, nunca para quem fez a ação. |
| "O progresso é mostrado em percentagem." | Sem regra de arredondamento nem caso sem tarefas. | Arredondado às unidades; 0% quando o projeto não tem tarefas. |
| "O administrador pode gerir utilizadores." | "Gerir" é vago e incluía apagar contas, que não foi pedido. | Só promover e despromover administradores (US-11). Apagar contas fica fora do MVP. |
| "Arrastar o cartão no Kanban muda o estado." | Arrastar e largar é uma decisão de interface ainda não confirmada. | "Mover o cartão para outra coluna", seja como for feito na interface. |
| "Sugestão: tarefas com etiquetas." | Funcionalidade não pedida. | Removida. |

## Pressupostos novos

- **P-23** Uma password tem pelo menos 8 caracteres. *(Valor comum; por confirmar.)*
- **P-24** O nome de um projeto tem no máximo 100 caracteres e o título de uma tarefa no máximo 200.
- **P-25** Quem faz uma ação nunca recebe notificação dela (ex.: atribuir uma tarefa a si próprio).
- **P-26** As notificações não lidas mantêm-se até o utilizador as abrir; não expiram no MVP.

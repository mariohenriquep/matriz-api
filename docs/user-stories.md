# User Stories — Matriz (MVP)

> Gerado no Exercício 3 da Ficha 01 (ver `docs/ai-log/ex03-user-stories.md`) a partir das decisões do Exercício 2.
> Prioridade MoSCoW: **M** = Must, **S** = Should. Critérios de aceitação em Given / When / Then (Dado / Quando / Então).
> Os itens com **[P-xx]** dependem de um pressuposto ainda não confirmado pelo cliente.

## Perfis

| Perfil | Resumo |
|--------|--------|
| **Visitante** | Sem sessão iniciada. |
| **Membro de projeto** | Trabalha nas tarefas dos projetos onde foi adicionado. |
| **Gestor de projeto** | Quem criou o projeto. Edita, arquiva e gere os membros desse projeto. Também é Membro. |
| **Administrador global** | Vê e gere todos os projetos e promove outros a administrador global. A primeira conta registada é administrador **[P-20]**. |

## Resumo do backlog

| ID | User Story | Funcionalidade (Ex. 2) | Prioridade |
|----|-----------|------------------------|-----------|
| US-01 | Registo e início de sessão | F-01 | M |
| US-02 | Criar projeto | F-02 | M |
| US-03 | Ver a lista dos meus projetos | F-03 | M |
| US-04 | Adicionar membro a um projeto | F-04 | M |
| US-05 | Remover membro de um projeto | F-04 | M |
| US-06 | Criar tarefa | F-05 | M |
| US-07 | Editar e eliminar tarefa | F-05 | M |
| US-08 | Atribuir responsável | F-06 | M |
| US-09 | Alterar o estado de uma tarefa | F-07 | M |
| US-10 | Listar e filtrar tarefas | F-08 | M |
| US-11 | Promover e despromover administradores | F-12 | M |
| US-12 | Ver o progresso do projeto | F-09 | M |
| US-13 | Quadro Kanban | F-13 | S |
| US-14 | Receber notificações | F-14 | S |
| US-15 | As minhas tarefas | F-10 | S |
| US-16 | Arquivar projeto | F-11 | S |

---

## US-01 — Registo e início de sessão (M)

**Como** visitante, **quero** criar uma conta e iniciar sessão, **para** aceder aos projetos da minha equipa.

1. **Dado** que não existe conta com `ana@exemplo.pt`, **quando** me registo com nome "Ana", esse email e uma password com 8 ou mais caracteres **[P-23]**, **então** a conta é criada e fico com sessão iniciada.
2. **Dado** que não existe nenhuma conta na Matriz, **quando** a Ana se regista, **então** a conta dela fica como administrador global **[P-20]**.
3. **Dado** que já existe conta com `ana@exemplo.pt`, **quando** alguém se tenta registar com `Ana@Exemplo.pt`, **então** o registo é recusado com "Já existe uma conta com este email".
4. **Dado** que tenho conta, **quando** inicio sessão com email ou password errados, **então** vejo "Credenciais inválidas", sem indicar qual dos dois está errado.
5. **Dado** que tenho sessão iniciada, **quando** termino a sessão, **então** deixo de conseguir abrir qualquer página de projeto sem voltar a iniciar sessão.

## US-02 — Criar projeto (M)

**Como** utilizador autenticado, **quero** criar um projeto, **para** organizar o trabalho da minha equipa.

1. **Dado** que tenho sessão iniciada, **quando** crio o projeto "Website 2027", **então** o projeto aparece na minha lista de projetos e eu fico como seu Gestor.
2. **Dado** que tenho sessão iniciada, **quando** tento criar um projeto sem nome ou só com espaços, **então** o projeto não é criado e vejo "O nome é obrigatório".
3. **Dado** que tenho sessão iniciada, **quando** tento criar um projeto com um nome de 101 caracteres **[P-24]**, **então** o projeto não é criado e vejo "O nome tem no máximo 100 caracteres".

## US-03 — Ver a lista dos meus projetos (M)

**Como** utilizador autenticado, **quero** ver os projetos em que participo, **para** escolher em qual vou trabalhar.

1. **Dado** que sou membro de P1 e P2 e não sou membro de P3, **quando** abro a lista de projetos, **então** vejo P1 e P2 e não vejo P3.
2. **Dado** que sou administrador global, **quando** abro a lista de projetos, **então** vejo todos os projetos ativos, incluindo os que não integro.
3. **Dado** que não participo em nenhum projeto, **quando** abro a lista, **então** vejo "Ainda não participas em nenhum projeto" e a opção "Criar projeto".

## US-04 — Adicionar membro a um projeto (M)

**Como** gestor de projeto, **quero** adicionar colegas ao projeto, **para** que possam trabalhar nas suas tarefas.

1. **Dado** que sou Gestor de P e `rui@exemplo.pt` tem conta, **quando** o adiciono a P, **então** P passa a aparecer na lista de projetos do Rui e ele recebe uma notificação (US-14).
2. **Dado** que sou administrador global e não sou membro de P, **quando** adiciono o Rui a P, **então** a operação é aceite como no critério 1.
3. **Dado** que sou Gestor de P, **quando** adiciono um email sem conta registada, **então** a operação é recusada com "Não existe utilizador com este email".
4. **Dado** que o Rui já é membro de P, **quando** o tento adicionar outra vez, **então** a operação é recusada com "Este utilizador já é membro do projeto".
5. **Dado** que sou só Membro de P, **quando** tento adicionar alguém, **então** a operação é recusada com "Sem permissão".

## US-05 — Remover membro de um projeto (M)

**Como** gestor de projeto, **quero** remover um membro, **para** controlar quem tem acesso ao projeto.

1. **Dado** que o Rui é membro de P e responsável por 2 tarefas, **quando** o removo, **então** ele deixa de ver P e as 2 tarefas ficam sem responsável.
2. **Dado** que sou o Gestor de P, **quando** me tento remover a mim próprio, **então** a operação é recusada com "O gestor não pode sair do projeto".

## US-06 — Criar tarefa (M)

**Como** membro de projeto, **quero** criar tarefas, **para** registar o trabalho a fazer.

1. **Dado** que sou membro de P, **quando** crio uma tarefa só com o título "Rever logótipo", **então** a tarefa fica em P com estado `Por fazer`, prioridade `Média`, sem responsável e comigo como criador.
2. **Dado** que sou membro de P, **quando** crio uma tarefa com título, descrição, prioridade `Alta`, responsável "Joana" e data limite 2026-11-15, **então** todos os campos ficam guardados com esses valores.
3. **Dado** que sou membro de P, **quando** crio uma tarefa sem título, **então** a tarefa não é criada e vejo "O título é obrigatório".
4. **Dado** que não sou membro de P nem administrador, **quando** tento criar uma tarefa em P, **então** a operação é recusada como se P não existisse (CE-07).

## US-07 — Editar e eliminar tarefa (M)

**Como** membro de projeto, **quero** editar ou eliminar uma tarefa, **para** manter a informação correta.

1. **Dado** que sou membro de P, **quando** altero o título da tarefa T para "Rever logótipo v2", **então** T passa a mostrar o novo título.
2. **Dado** que criei a tarefa T, **quando** a elimino e confirmo, **então** T deixa de aparecer na lista e no quadro de P.
3. **Dado** que sou Gestor de P ou administrador global, **quando** elimino uma tarefa criada por outra pessoa, **então** a operação é aceite.
4. **Dado** que sou só Membro de P e não criei T, **quando** tento eliminar T, **então** a operação é recusada com "Sem permissão".

## US-08 — Atribuir responsável (M)

**Como** membro de projeto, **quero** atribuir uma tarefa a um membro, **para** que fique claro quem a faz.

1. **Dado** que a tarefa T pertence a P e o Rui é membro de P, **quando** atribuo T ao Rui, **então** o Rui aparece como responsável de T e recebe uma notificação.
2. **Dado** que a Joana não é membro de P, **quando** tento atribuir T à Joana, **então** a operação é recusada com "O responsável tem de ser membro do projeto".
3. **Dado** que T está atribuída ao Rui, **quando** removo o responsável, **então** T fica "Sem responsável".

## US-09 — Alterar o estado de uma tarefa (M)

**Como** membro de projeto, **quero** mudar o estado de uma tarefa, **para** mostrar como o trabalho está a evoluir.

1. **Dado** que T está `Por fazer`, **quando** a mudo para `Em curso`, **então** T fica `Em curso` e o progresso do projeto é recalculado.
2. **Dado** que T está `Concluída`, **quando** a mudo para `Por fazer`, **então** a alteração é aceite (pode-se reabrir tarefas).
3. **Dado** que tento aplicar a T um estado que não é um dos três (ex.: `Bloqueada`), **então** a operação é recusada e T mantém o estado.
4. **Dado** que T está atribuída ao Rui e eu não sou o Rui, **quando** mudo o estado de T, **então** o Rui recebe uma notificação.

## US-10 — Listar e filtrar tarefas (M)

**Como** membro de projeto, **quero** ver as tarefas do projeto e filtrá-las, **para** encontrar o que me interessa.

1. **Dado** que P tem 3 tarefas `Por fazer` e 2 `Concluída`, **quando** filtro por `Por fazer`, **então** vejo exatamente as 3 tarefas `Por fazer`.
2. **Dado** que filtro por responsável "Rui" e estado `Em curso`, **então** só vejo tarefas que cumprem os dois filtros.
3. **Dado** que nenhuma tarefa cumpre os filtros, **então** vejo "Sem tarefas para os filtros escolhidos".

## US-11 — Promover e despromover administradores (M)

**Como** administrador global, **quero** dar ou retirar o papel de administrador global a outros utilizadores, **para** partilhar a gestão da Matriz.

1. **Dado** que sou administrador global e o Rui não é, **quando** promovo o Rui, **então** o Rui passa a ver todos os projetos ativos.
2. **Dado** que o Rui é administrador global, **quando** o despromovo, **então** ele passa a ver só os projetos onde é membro.
3. **Dado** que sou o único administrador global, **quando** me tento despromover, **então** a operação é recusada com "Tem de existir pelo menos um administrador global".
4. **Dado** que não sou administrador global, **quando** tento promover alguém, **então** a operação é recusada com "Sem permissão".

## US-12 — Ver o progresso do projeto (M)

**Como** membro de projeto, **quero** ver o progresso do projeto, **para** saber quanto trabalho falta.

1. **Dado** que P tem 10 tarefas (3 `Por fazer`, 3 `Em curso`, 4 `Concluída`), **quando** abro o painel de P, **então** vejo 3 / 3 / 4 e "40% concluído".
2. **Dado** que P não tem tarefas, **quando** abro o painel, **então** vejo "0 tarefas" e "0% concluído", sem erro.
3. **Dado** que P tem 3 tarefas e 1 está `Concluída`, **então** vejo "33% concluído" (arredondado às unidades).

## US-13 — Quadro Kanban (S)

**Como** membro de projeto, **quero** ver as tarefas num quadro com uma coluna por estado, **para** perceber de relance como está o trabalho.

1. **Dado** que P tem tarefas nos três estados, **quando** abro o quadro de P, **então** vejo três colunas (`Por fazer`, `Em curso`, `Concluída`) e cada tarefa aparece apenas na coluna do seu estado.
2. **Dado** que a tarefa T está na coluna `Por fazer`, **quando** a movo para a coluna `Em curso`, **então** o estado de T passa a `Em curso`, tal como na US-09.
3. **Dado** que aplico um filtro por responsável, **quando** vejo o quadro, **então** só aparecem as tarefas desse responsável.

## US-14 — Receber notificações (S)

**Como** membro de projeto, **quero** ser notificado quando há trabalho para mim, **para** não ter de procurar alterações manualmente.

As notificações são dentro da aplicação **[P-21]**.

1. **Dado** que o Rui me atribui a tarefa T, **quando** abro a Matriz, **então** vejo o contador de notificações não lidas aumentado em 1 e uma notificação "Rui atribuiu-te a tarefa T em P".
2. **Dado** que o Rui me adiciona ao projeto P, **então** recebo a notificação "Rui adicionou-te ao projeto P".
3. **Dado** que me atribuo a mim próprio uma tarefa, **então** não recebo notificação **[P-25]**.
4. **Dado** que tenho 3 notificações não lidas, **quando** abro uma, **então** fica marcada como lida e o contador passa a 2.

## US-15 — As minhas tarefas (S)

**Como** membro, **quero** ver todas as tarefas atribuídas a mim, **para** organizar o meu dia.

1. **Dado** que tenho tarefas atribuídas em P1 e P2, **quando** abro "As minhas tarefas", **então** vejo as tarefas dos dois projetos, cada uma com o nome do projeto.
2. **Dado** que P2 está arquivado, **então** as tarefas de P2 não aparecem.

## US-16 — Arquivar projeto (S)

**Como** gestor de projeto, **quero** arquivar um projeto terminado, **para** manter o histórico sem ocupar a lista de projetos ativos.

1. **Dado** que sou Gestor de P (ou administrador global), **quando** arquivo P, **então** P sai da lista de projetos ativos e aparece em "Projetos arquivados".
2. **Dado** que P está arquivado, **quando** qualquer pessoa tenta criar, editar ou mudar o estado de uma tarefa de P, **então** a operação é recusada com "O projeto está arquivado".
3. **Dado** que P está arquivado, **quando** o reativo, **então** P volta à lista de projetos ativos.

---

## Casos excecionais e limites

| ID | Situação | Comportamento esperado | US |
|----|----------|------------------------|----|
| CE-01 | Membro removido com tarefas atribuídas. | As tarefas ficam sem responsável; não são apagadas. | US-05 |
| CE-02 | O Gestor tenta sair do próprio projeto. | Recusado. | US-05 |
| CE-03 | O último administrador global tenta despromover-se. | Recusado: tem de existir sempre um. | US-11 |
| CE-04 | Projeto sem tarefas. | Progresso 0%, sem erro (sem divisão por zero). | US-12 |
| CE-05 | Ação de escrita num projeto arquivado. | Recusada; o projeto fica só de leitura. | US-16 |
| CE-06 | Data limite no passado ao criar tarefa. | Aceite. A tarefa aparece como "Atrasada" enquanto não estiver `Concluída`. **[P]** | US-06 |
| CE-07 | Utilizador abre um projeto onde não é membro (e não é admin). | Resposta "não encontrado", sem revelar que o projeto existe. | US-03, US-06 |
| CE-08 | Email com maiúsculas diferentes. | Tratado como o mesmo email. | US-01, US-04 |
| CE-09 | Duas pessoas editam a mesma tarefa ao mesmo tempo. | Prevalece a última gravação no MVP. **[P]** | US-07, US-09 |
| CE-10 | Mover uma tarefa no Kanban para a coluna onde já está. | Nada muda e não se gera notificação. | US-13 |
| CE-11 | O utilizador faz uma ação sobre si próprio (ex.: atribuir-se uma tarefa). | Sem notificação. **[P-25]** | US-14 |
| CE-12 | Responsável é removido do projeto depois de receber a notificação. | A notificação mantém-se, mas ao abri-la vê "Já não tens acesso a este projeto". | US-05, US-14 |

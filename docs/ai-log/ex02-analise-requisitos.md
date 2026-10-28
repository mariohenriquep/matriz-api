# Ficha 01 — Exercício 2: Análise de Requisitos com IA

> Ferramenta de IA usada: Claude (Claude Code). O prompt segue a estrutura escolhida no Exercício 1 (B). A resposta foi revista e corrigida por mim antes de ser registada aqui.

## Prompt

```text
# Goal
Definir uma primeira proposta do âmbito funcional da Matriz e de um MVP, com todas as
decisões e pressupostos identificados.

# Context
Descrição do cliente: "Precisamos de uma aplicação onde as nossas equipas possam organizar
projetos, gerir tarefas e acompanhar o progresso do trabalho."
No exercício anterior identificámos 5 factos e 12 perguntas de clarificação (em anexo).
Não é possível fazer estas perguntas ao cliente nesta fase.

# Constraints
- Atua como analista de requisitos.
- Não proponhas tecnologias nem código.
- Cada resposta a uma pergunta de clarificação é um PRESSUPOSTO e deve ser marcada como tal.
- Prefere a opção mais simples que ainda cumpra a descrição do cliente.
- O MVP deve ser o mínimo que permite organizar projetos, gerir tarefas e ver progresso.
- Português de Portugal.

# Task
1. Para cada pergunta de clarificação, propõe uma resposta razoável (pressuposto) e o
   impacto se estiver errada.
2. Identifica os tipos de utilizadores.
3. Propõe as funcionalidades, cada uma com justificação e prioridade MoSCoW.
4. Separa as funcionalidades essenciais (MVP) das futuras.
5. Define o âmbito do MVP num parágrafo e num fluxo de ponta a ponta.

# Validation
- Cada funcionalidade do MVP liga-se a pelo menos um dos três objetivos do cliente
  (organizar projetos, gerir tarefas, acompanhar progresso).
- Nenhuma funcionalidade "Could" ou "Won't" está no MVP.
- Todos os pressupostos estão marcados.
```

---

## 1. Respostas às perguntas de clarificação (pressupostos)

Todas as respostas abaixo são **pressupostos** (P-xx). Não foram confirmadas pelo cliente.

| # | Pergunta (Ex. 1) | Pressuposto adotado | Impacto se estiver errado |
|---|------------------|---------------------|---------------------------|
| 1 | Quem vai usar e quantos? | **P-01** Equipas internas, pequenas a médias (até ~50 pessoas e ~100 projetos ativos). | Escala, desempenho e arquitetura. |
| 2 | Há papéis com permissões diferentes? | **P-02** ✅ *Confirmado.* Dois tipos de utilizador: **Administrador global** e **Membro**. Os administradores criam os projetos, adicionam membros e podem promover outros utilizadores a administrador global. | Modelo de permissões. |
| 3 | Uma tarefa pertence sempre a um projeto? Que informação tem? | **P-13** Sim, sempre. Título (obrigatório), descrição, prioridade, responsável, data limite e estado. | Modelo de dados. |
| 4 | Que estados e o fluxo é igual para todos? | **P-04** ✅ *Confirmado.* Três estados fixos (`Por fazer`, `Em curso`, `Concluída`), iguais em todos os projetos; qualquer transição é permitida. | Regras das tarefas e cálculo do progresso. |
| 5 | O que é "acompanhar o progresso"? | **P-05** Número de tarefas por estado e % de tarefas concluídas, por projeto, visível a todos os membros. | Painel do projeto. |
| 6 | Mais do que um responsável por tarefa? | **P-07** Não, no máximo um. | Modelo de dados. |
| 7 | Cada pessoa vê só os seus projetos? | **P-14** Sim, só os projetos onde é membro. | Privacidade e permissões. |
| 8 | O que é indispensável no primeiro dia? | **P-15** ✅ *Confirmado.* Projetos, membros, tarefas, atribuição, estados, progresso, quadro Kanban e notificações. Comentários e anexos ficam para depois. | Âmbito do MVP. |
| 9 | Em que dispositivos? | **P-16** Aplicação web, usada em computador e tablet. Sem app móvel nativa. | Interface. |
| 10 | Prazo e orçamento? | **P-17** Desconhecido. Assume-se o calendário das aulas da UC. | Planeamento. |
| 11 | Dados pessoais ou confidenciais? | **P-18** Só nome e email dos utilizadores. As tarefas podem ter informação interna, por isso o acesso é restrito aos membros. Aplica-se o RGPD. | Segurança e privacidade. |
| 12 | Integrações com sistemas existentes? | **P-19** Nenhuma no MVP. Autenticação própria (email e palavra-passe). | Autenticação e âmbito. |

Pressupostos adicionais que surgiram durante a análise:

- **P-03** ✅ *Confirmado.* Só administradores globais criam projetos.
- **P-20** A primeira conta registada na Matriz é administrador global (é quem cria os primeiros projetos). Tem de existir sempre pelo menos um administrador global.
- **P-21** As notificações são dentro da aplicação (in-app) e disparam quando: me atribuem uma tarefa, me adicionam a um projeto, ou muda o estado de uma tarefa que me está atribuída. Notificações por email ficam fora do MVP.
- **P-06** Prioridades: `Baixa`, `Média` (por defeito), `Alta`.
- **P-08** Só se adicionam ao projeto pessoas que já têm conta (sem convites por email).
- **P-10** ✅ *Confirmado.* Os projetos não se apagam, arquivam-se.

## 2. Tipos de utilizadores

| Tipo | O que faz | Pressuposto |
|------|-----------|-------------|
| **Visitante** | Regista-se ou inicia sessão. | — |
| **Administrador global** | Cria, edita e arquiva projetos; adiciona e remove membros em qualquer projeto; promove outros utilizadores a administrador global. Vê todos os projetos. Também faz tudo o que um Membro faz. | P-02, P-03, P-20 |
| **Membro** | Nos projetos onde foi adicionado: cria, edita e atribui tarefas, muda estados, consulta tarefas, quadro e progresso. Recebe notificações. | P-02, P-21 |

## 3. Funcionalidades propostas

Prioridade MoSCoW: **M** = Must, **S** = Should, **C** = Could, **W** = Won't (agora).

| ID | Funcionalidade | Objetivo do cliente | Justificação | Prioridade | MVP |
|----|----------------|--------------------|--------------|-----------|-----|
| F-01 | Registo e início/fim de sessão | todos | Sem identidade não há controlo de acesso nem atribuição de tarefas. | M | ✅ |
| F-02 | Criar e editar projeto | Organizar projetos | É o núcleo de "organizar projetos". | M | ✅ |
| F-03 | Listar os meus projetos | Organizar projetos | Ponto de entrada da aplicação. | M | ✅ |
| F-04 | Adicionar e remover membros | Organizar projetos | Sem membros não há trabalho em equipa. | M | ✅ |
| F-05 | Criar, editar e eliminar tarefas | Gerir tarefas | Núcleo de "gerir tarefas". | M | ✅ |
| F-06 | Atribuir responsável | Gerir tarefas | Saber quem faz o quê. | M | ✅ |
| F-07 | Mudar estado da tarefa | Gerir tarefas / Progresso | Sem estados não há como medir progresso. | M | ✅ |
| F-08 | Listar tarefas com filtros (estado, responsável) | Gerir tarefas | Encontrar trabalho num projeto com muitas tarefas. | M | ✅ |
| F-09 | Painel de progresso do projeto | Acompanhar progresso | Cumpre diretamente o 3.º objetivo. | M | ✅ |
| F-10 | "As minhas tarefas" (todas as atribuídas a mim) | Gerir tarefas | Ajuda cada pessoa a focar-se, mas não é indispensável. | S | ✅ |
| F-11 | Arquivar projeto | Organizar projetos | Mantém histórico sem apagar dados (P-10). | S | ✅ |
| F-12 | Administração global: promover/despromover administradores | Organizar projetos | Pedido do Mário: existe um administrador global que cria os projetos e pode promover outros. | M | ✅ |
| F-13 | Quadro Kanban (colunas por estado) | Gerir tarefas / Progresso | Mostra o estado do trabalho de forma visual. Subiu para Should por decisão do Mário. | S | ✅ |
| F-14 | Notificações in-app (atribuição, adição a projeto, mudança de estado) | Gerir tarefas | Pedido do Mário: os membros devem saber quando há trabalho para eles (P-21). | S | ✅ |
| F-15 | Comentários nas tarefas | Gerir tarefas | Útil para colaboração; não pedido explicitamente. | C | ❌ |
| F-16 | Notificações por email | Gerir tarefas | Exige um serviço de email externo; as in-app (F-14) cobrem o MVP. | C | ❌ |
| F-17 | Anexos | Gerir tarefas | Exige armazenamento de ficheiros e mais segurança. | W | ❌ |
| F-18 | Sprints, epics, subtarefas, dependências | Organizar projetos | Típico do Jira, mas nada na descrição o exige. | W | ❌ |
| F-19 | Estados personalizados por projeto | Gerir tarefas | Contraria P-04 (três estados fixos confirmados). | W | ❌ |
| F-20 | Relatórios e gráficos históricos | Acompanhar progresso | O painel simples (F-09) chega para começar. | W | ❌ |
| F-21 | SSO / login com Google ou Microsoft | todos | Contraria P-19; depende da empresa do cliente. | W | ❌ |
| F-22 | Organizações / várias empresas na mesma instalação | todos | Só faz sentido com vários clientes. | W | ❌ |

## 4. Âmbito do MVP

**Em resumo:** o MVP da Matriz permite que um administrador global crie projetos e junte colegas como membros, e que a equipa crie, atribua e acompanhe tarefas através de três estados, numa lista ou num quadro Kanban, recebendo notificações quando há trabalho para si e vendo em cada projeto quantas tarefas estão em cada estado e a percentagem concluída. Inclui F-01 a F-14.

**Fluxo de ponta a ponta:**

1. A Ana é a primeira a registar-se e fica administradora global.
2. Cria o projeto "Website 2027".
3. O Rui e a Joana registam-se; a Ana adiciona-os ao projeto como Membros. Cada um recebe uma notificação.
4. O Rui cria a tarefa "Rever logótipo" e atribui-a à Joana, que recebe uma notificação.
5. A Joana vê a tarefa em "As minhas tarefas" e no quadro Kanban, muda-a para `Em curso` e depois para `Concluída`.
6. A Ana abre o painel do projeto e vê o progresso atualizado.
7. A Ana promove o Rui a administrador global, para que ele também possa criar projetos.

## 5. Validação da resposta

| Critério do prompt | Resultado |
|--------------------|-----------|
| Cada funcionalidade do MVP liga-se a um objetivo do cliente. | ✅ ver coluna "Objetivo do cliente". |
| Nenhuma C/W no MVP. | ✅ F-15 a F-22 fora. |
| Pressupostos marcados. | ✅ P-01 a P-21; os confirmados pelo Mário estão assinalados com ✅. |

## 6. Revisão humana (Human-in-the-Loop)

A primeira versão desta análise foi revista pelo Mário. Decisões tomadas:

| Proposta da IA | Decisão do Mário | Resultado |
|----------------|------------------|-----------|
| Sem administrador global; papéis Gestor/Membro por projeto. | **Alterada.** Existe um administrador global, que cria os projetos e pode promover outros a administrador global. | P-02, P-03, P-20; nova F-12. O papel "Gestor de projeto" deixou de existir. |
| Quadro Kanban como Could, fora do MVP. | **Alterada.** Passa a Should. | F-13 entra no MVP. |
| Sem notificações no MVP. | **Alterada.** O MVP tem notificações. | F-14 (in-app) entra no MVP; email fica como F-16 (Could). |
| Arquivar em vez de apagar projetos. | **Aceite.** | P-10, F-11. |
| Três estados fixos, qualquer transição permitida. | **Aceite.** | P-04. |

Pressupostos novos que esta revisão criou e ainda estão por confirmar: **P-20** (a primeira conta é administradora) e **P-21** (notificações in-app e os eventos que as disparam).

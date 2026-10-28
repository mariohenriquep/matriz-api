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
| 2 | Há papéis com permissões diferentes? | **P-02** Dois papéis **por projeto**: Gestor e Membro. Sem administrador global no MVP. | Modelo de permissões. |
| 3 | Uma tarefa pertence sempre a um projeto? Que informação tem? | **P-13** Sim, sempre. Título (obrigatório), descrição, prioridade, responsável, data limite e estado. | Modelo de dados. |
| 4 | Que estados e o fluxo é igual para todos? | **P-04** Três estados fixos (`Por fazer`, `Em curso`, `Concluída`), iguais em todos os projetos; qualquer transição é permitida. | Regras das tarefas e cálculo do progresso. |
| 5 | O que é "acompanhar o progresso"? | **P-05** Número de tarefas por estado e % de tarefas concluídas, por projeto, visível a todos os membros. | Painel do projeto. |
| 6 | Mais do que um responsável por tarefa? | **P-07** Não, no máximo um. | Modelo de dados. |
| 7 | Cada pessoa vê só os seus projetos? | **P-14** Sim, só os projetos onde é membro. | Privacidade e permissões. |
| 8 | O que é indispensável no primeiro dia? | **P-15** Projetos, membros, tarefas, atribuição, estados e progresso. Comentários, anexos e notificações ficam para depois. | Âmbito do MVP. |
| 9 | Em que dispositivos? | **P-16** Aplicação web, usada em computador e tablet. Sem app móvel nativa. | Interface. |
| 10 | Prazo e orçamento? | **P-17** Desconhecido. Assume-se o calendário das aulas da UC. | Planeamento. |
| 11 | Dados pessoais ou confidenciais? | **P-18** Só nome e email dos utilizadores. As tarefas podem ter informação interna, por isso o acesso é restrito aos membros. Aplica-se o RGPD. | Segurança e privacidade. |
| 12 | Integrações com sistemas existentes? | **P-19** Nenhuma no MVP. Autenticação própria (email e palavra-passe). | Autenticação e âmbito. |

Pressupostos adicionais que surgiram durante a análise:

- **P-03** Qualquer utilizador autenticado pode criar projetos (e fica Gestor).
- **P-06** Prioridades: `Baixa`, `Média` (por defeito), `Alta`.
- **P-08** Só se adicionam ao projeto pessoas que já têm conta (sem convites por email).
- **P-10** Os projetos não se apagam, arquivam-se.

## 2. Tipos de utilizadores

| Tipo | O que faz | Pressuposto |
|------|-----------|-------------|
| **Visitante** | Regista-se ou inicia sessão. | — |
| **Gestor de projeto** | Cria e edita o projeto, gere membros, arquiva o projeto. Também faz tudo o que um Membro faz. | P-02, P-03 |
| **Membro de projeto** | Cria, edita e atribui tarefas, muda estados, consulta tarefas e progresso. | P-02 |

Os papéis são por projeto: a mesma pessoa pode ser Gestor num projeto e Membro noutro.

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
| F-12 | Quadro Kanban (colunas por estado) | Gerir tarefas | Visual, mas a lista filtrada já cumpre o objetivo. | C | ❌ |
| F-13 | Comentários nas tarefas | Gerir tarefas | Útil para colaboração; não pedido explicitamente. | C | ❌ |
| F-14 | Notificações (email/in-app) | Gerir tarefas | Útil, mas aumenta muito a complexidade. | C | ❌ |
| F-15 | Anexos | Gerir tarefas | Exige armazenamento de ficheiros e mais segurança. | W | ❌ |
| F-16 | Sprints, epics, subtarefas, dependências | Organizar projetos | Típico do Jira, mas nada na descrição o exige. | W | ❌ |
| F-17 | Estados personalizados por projeto | Gerir tarefas | Contraria P-04; só se o cliente pedir. | W | ❌ |
| F-18 | Relatórios e gráficos históricos | Acompanhar progresso | O painel simples (F-09) chega para começar. | W | ❌ |
| F-19 | SSO / login com Google ou Microsoft | todos | Contraria P-19; depende da empresa do cliente. | W | ❌ |
| F-20 | Administrador global e organizações | todos | Contraria P-02; só faz sentido com vários clientes. | W | ❌ |

## 4. Âmbito do MVP

**Em resumo:** o MVP da Matriz permite que um utilizador registado crie projetos, junte colegas como membros, e que a equipa crie, atribua e acompanhe tarefas através de três estados, vendo em cada projeto quantas tarefas estão em cada estado e a percentagem concluída. Inclui F-01 a F-11.

**Fluxo de ponta a ponta:**

1. A Ana regista-se e inicia sessão.
2. Cria o projeto "Website 2027" e fica Gestora.
3. Adiciona o Rui e a Joana como Membros.
4. O Rui cria a tarefa "Rever logótipo" e atribui-a à Joana.
5. A Joana vê a tarefa em "As minhas tarefas", muda-a para `Em curso` e depois para `Concluída`.
6. A Ana abre o painel do projeto e vê o progresso atualizado.

## 5. Validação da resposta

| Critério do prompt | Resultado |
|--------------------|-----------|
| Cada funcionalidade do MVP liga-se a um objetivo do cliente. | ✅ ver coluna "Objetivo do cliente". |
| Nenhuma C/W no MVP. | ✅ F-12 a F-20 fora. |
| Pressupostos marcados. | ✅ P-01 a P-19. |

## 6. Decisões a validar (Human-in-the-Loop)

Estas escolhas não são óbvias e precisam da tua confirmação antes de seguirmos para o Exercício 3:

- **Sem administrador global** (F-20 fora do MVP). Nada na descrição o exige e acrescentaria permissões a definir.
- **Quadro Kanban como Could** (F-12), apesar de ser habitual nestas ferramentas. A lista com filtros já cumpre "gerir tarefas".
- **Sem notificações no MVP** (F-14). Exigem um serviço de email e não são essenciais ao fluxo.
- **Arquivar em vez de apagar projetos** (F-11, P-10), para não perder histórico.
- **Três estados fixos com qualquer transição permitida** (P-04).

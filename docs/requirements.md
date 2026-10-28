# Requisitos — Matriz

> Ficha 01, Exercício 4. Consolida a análise do Exercício 2 e o backlog do Exercício 3.
> **[P-xx]** = depende de um pressuposto (lista completa em `docs/constraints.md`). ✅ = decisão confirmada pelo Mário (atuando como cliente).

## 1. Visão

A Matriz é uma aplicação web onde equipas de trabalho organizam projetos, gerem tarefas e acompanham o progresso do trabalho, como o Jira, o Azure DevOps ou o Trello, mas focada no essencial.

**Descrição original do cliente (único requisito confirmado pelo cliente):**
> "Precisamos de uma aplicação onde as nossas equipas possam organizar projetos, gerir tarefas e acompanhar o progresso do trabalho."

## 2. Objetivos

| ID | Objetivo | Origem |
|----|----------|--------|
| OBJ-1 | Organizar o trabalho em projetos com membros. | Cliente |
| OBJ-2 | Criar, atribuir e atualizar tarefas dentro de cada projeto. | Cliente |
| OBJ-3 | Dar visibilidade sobre o progresso de cada projeto. | Cliente |
| OBJ-4 | Garantir que cada pessoa só acede aos projetos que lhe dizem respeito. | Pressuposto [P-14] |

## 3. Tipos de utilizadores

| Perfil | Descrição | Origem |
|--------|-----------|--------|
| Visitante | Sem sessão iniciada. Só pode registar-se ou iniciar sessão. | — |
| Membro de projeto | Trabalha nas tarefas dos projetos onde foi adicionado. | ✅ P-02 |
| Gestor de projeto | Quem criou o projeto. Edita, arquiva e reativa o projeto e gere os seus membros. Também é Membro. | ✅ P-03, P-22 |
| Administrador global | Vê e gere todos os projetos e promove ou despromove outros administradores globais. A primeira conta registada é administrador. | ✅ P-02, [P-20] |

Qualquer utilizador autenticado pode criar projetos ✅. O papel de Gestor e de Membro é por projeto; o de Administrador global é para toda a Matriz.

## 4. Requisitos funcionais

Prioridade MoSCoW. A coluna "US" liga cada requisito ao backlog em `docs/user-stories.md`.

| ID | Requisito | Prioridade | Justificação | US |
|----|-----------|-----------|--------------|----|
| RF-01 | Registar conta com nome, email e password; iniciar e terminar sessão. | M | Sem identidade não há controlo de acesso nem atribuição. | US-01 |
| RF-02 | A primeira conta registada fica como administrador global. | M | Garante que existe sempre um administrador [P-20]. | US-01 |
| RF-03 | Criar projeto (nome obrigatório, descrição opcional); o criador fica Gestor. | M | OBJ-1. | US-02 |
| RF-04 | Editar nome e descrição do projeto (Gestor ou administrador). | M | Os projetos evoluem. | US-02 |
| RF-05 | Listar os projetos do utilizador; o administrador vê todos. | M | Ponto de entrada da aplicação. | US-03 |
| RF-06 | Adicionar um utilizador registado a um projeto, por email (Gestor ou administrador). | M | Trabalho em equipa. | US-04 |
| RF-07 | Remover membro de um projeto; as suas tarefas ficam sem responsável. | M | Controlo de acesso. | US-05 |
| RF-08 | Criar tarefa (título obrigatório; descrição, prioridade, responsável e data limite opcionais). | M | OBJ-2. | US-06 |
| RF-09 | Editar tarefa; eliminar tarefa (criador, Gestor ou administrador). | M | Manter a informação correta. | US-07 |
| RF-10 | Atribuir ou retirar o responsável de uma tarefa (tem de ser membro do projeto). | M | Saber quem faz o quê. | US-08 |
| RF-11 | Alterar o estado da tarefa entre `Por fazer`, `Em curso` e `Concluída`, com qualquer transição. | M | Base do progresso (OBJ-3). ✅ P-04 | US-09 |
| RF-12 | Listar tarefas de um projeto com filtros por estado e responsável. | M | Encontrar trabalho. | US-10 |
| RF-13 | Promover e despromover administradores globais; tem de existir sempre pelo menos um. | M | Pedido do cliente (Mário). ✅ | US-11 |
| RF-14 | Painel de progresso: tarefas por estado e % concluída. | M | OBJ-3. | US-12 |
| RF-15 | Quadro Kanban com uma coluna por estado; mover uma tarefa de coluna muda o estado. | S | Visão rápida do trabalho. ✅ | US-13 |
| RF-16 | Notificações in-app quando: me atribuem uma tarefa, me adicionam a um projeto, ou muda o estado de uma tarefa minha. Contador de não lidas. | S | Pedido do cliente (Mário). ✅ [P-21] | US-14 |
| RF-17 | Lista "As minhas tarefas" com as tarefas atribuídas ao utilizador em todos os projetos ativos. | S | Foco individual. | US-15 |
| RF-18 | Arquivar e reativar projeto; um projeto arquivado fica só de leitura. | S | Não perder histórico. ✅ P-10 | US-16 |

## 5. Requisitos não funcionais

| ID | Categoria | Requisito | Prioridade |
|----|-----------|-----------|-----------|
| RNF-01 | Segurança | Passwords guardadas apenas com hash forte e salt. | M |
| RNF-02 | Segurança | Todas as operações, exceto registo e login, exigem sessão autenticada; as sessões expiram. | M |
| RNF-03 | Autorização | As permissões (Membro, Gestor, Administrador) são verificadas no servidor em cada pedido. | M |
| RNF-04 | Segurança | Comunicação cifrada (HTTPS) em produção. | M |
| RNF-05 | Privacidade | Dados pessoais limitados a nome e email; cumprimento do RGPD [P-18]. | M |
| RNF-06 | Validação | Todos os dados de entrada são validados no servidor, com mensagens de erro em português. | M |
| RNF-07 | Usabilidade | Interface em português, utilizável em computador e tablet [P-09, P-16]. | S |
| RNF-08 | Desempenho | A lista de tarefas de um projeto com 500 tarefas responde em menos de 1 segundo [P-12]. | S |
| RNF-09 | Escala | Suporta até ~50 utilizadores e ~100 projetos ativos [P-01]. | S |
| RNF-10 | Manutenibilidade | Código versionado em Git, com testes automáticos para as regras de negócio. | S |

## 6. Âmbito do MVP

O MVP inclui **RF-01 a RF-18** e permite este fluxo de ponta a ponta:

1. A primeira pessoa regista-se e fica administradora global.
2. Outros utilizadores registam-se; qualquer um cria projetos e fica Gestor.
3. O Gestor adiciona colegas ao projeto, que recebem uma notificação.
4. Os membros criam tarefas, atribuem responsáveis e mudam estados, na lista ou no quadro Kanban.
5. Todos os membros veem o progresso do projeto; o administrador vê todos os projetos.
6. Quando o projeto termina, o Gestor arquiva-o.

## 7. Fora do âmbito inicial

- Comentários e menções nas tarefas.
- Notificações por email ou push.
- Anexos.
- Sprints, epics, subtarefas e dependências.
- Estados personalizados por projeto.
- Relatórios avançados e gráficos históricos.
- Login com SSO, Google ou Microsoft; recuperação de password por email.
- Várias organizações/empresas na mesma instalação.
- Convites por email a pessoas sem conta.
- Apagar contas de utilizador e gerir utilizadores (além de promover/despromover administradores).
- Etiquetas, registo de tempo, estimativas.
- Aplicação móvel nativa e integrações com outros sistemas.
- Histórico de alterações das tarefas.

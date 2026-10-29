# Requisitos — Matriz

> Ficha 01, Exercício 4. **Atualizado na Ficha 03, Exercício 1:** conciliação com o âmbito da Ficha 03 (RF-05, RF-10, RF-12, RF-20, RF-21) e indicação do que fica como extra (ver `docs/implementation-plan.md` §1). Consolida a análise do Exercício 2 e o backlog do Exercício 3.
> **[P-xx]** = depende de um pressuposto (lista completa em `docs/constraints.md`). ✅ = decisão confirmada pelo Mário.
> Neste trabalho académico, o Mário faz o papel de cliente: as decisões dele contam como confirmações do cliente, além da frase original abaixo.

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
| Administrador global | Vê e gere todos os projetos e promove ou despromove outros administradores globais. O primeiro administrador é criado na instalação. | ✅ P-02, ✅ P-20 |

Qualquer utilizador autenticado pode criar projetos ✅. O papel de Gestor e de Membro é por projeto; o de Administrador global é para toda a Matriz.

## 4. Requisitos funcionais

Prioridade MoSCoW. A coluna "US" liga cada requisito ao backlog em `docs/user-stories.md`.

| ID | Requisito | Prioridade | Justificação | US |
|----|-----------|-----------|--------------|----|
| RF-01 | Iniciar e terminar sessão e consultar o utilizador autenticado. Na Ficha 03 usam-se contas de demonstração criadas por seed; o registo público fica como extra (C-06). | M | Sem identidade não há controlo de acesso nem atribuição. | US-01 |
| RF-02 | O primeiro administrador global é criado na instalação, por configuração, e não pelo primeiro registo. | M | Garante que existe um administrador sem permitir que um estranho o seja primeiro (Ex. 6, R-12). ✅ P-20 | US-01 |
| RF-03 | Criar projeto (nome obrigatório, descrição opcional); o criador fica Gestor. | M | OBJ-1. | US-02 |
| RF-04 | Editar nome e descrição do projeto (Gestor ou administrador). | M | Os projetos evoluem. | US-02 |
| RF-05 | Listar os projetos ativos do utilizador, com contagens de tarefas por estado e **pesquisa pelo nome** (C-04); o administrador vê todos os ativos. Os arquivados aparecem numa lista à parte, "Projetos arquivados", com a mesma regra de acesso. | M | Ponto de entrada da aplicação. | US-03 |
| RF-06 | Adicionar um utilizador registado a um projeto, por email (Gestor ou administrador). | M | Trabalho em equipa. | US-04 |
| RF-07 | Remover membro de um projeto (Gestor ou administrador); as suas tarefas ficam sem responsável. O Gestor não pode ser removido nem sair do projeto. | M | Controlo de acesso. | US-05 |
| RF-08 | Criar tarefa (título obrigatório; descrição, prioridade, responsável e data limite opcionais). Uma tarefa com data limite ultrapassada e não concluída aparece como "Atrasada" (✅ P-27). | M | OBJ-2. | US-06 |
| RF-09 | Editar tarefa; eliminar tarefa (criador, Gestor ou administrador). | M | Manter a informação correta. | US-07 |
| RF-10 | Atribuir **um ou mais responsáveis** a uma tarefa e remover cada atribuição (C-01). Cada responsável tem de ser membro do projeto e não pode ser atribuído duas vezes; um administrador global que não seja membro não pode ser responsável. | M | Saber quem faz o quê. | US-08 |
| RF-11 | Alterar o estado da tarefa entre `Por fazer`, `Em curso` e `Concluída`, com qualquer transição. | M | Base do progresso (OBJ-3). ✅ P-04 | US-09 |
| RF-12 | Listar tarefas de um projeto com filtros por estado, **prioridade** (C-05) e responsável. | M | Encontrar trabalho. | US-10 |
| RF-13 | Promover e despromover administradores globais; tem de existir sempre pelo menos um. | M | Pedido do cliente (Mário). ✅ | US-11 |
| RF-14 | Painel de progresso: tarefas por estado e % concluída. | M | OBJ-3. | US-12 |
| RF-15 | Quadro Kanban com uma coluna por estado; mover uma tarefa de coluna muda o estado. | S | Visão rápida do trabalho. ✅ | US-13 |
| RF-16 | Notificações in-app quando: me atribuem uma tarefa, me adicionam a um projeto, ou muda o estado de uma tarefa minha. Contador de não lidas. | S | Pedido do cliente (Mário). ✅ [P-21] | US-14 |
| RF-17 | Lista "As minhas tarefas" com as tarefas atribuídas ao utilizador em todos os projetos ativos. | S | Ver o trabalho de todos os projetos num só sítio, o que o filtro do RF-12 não permite (só filtra dentro de um projeto). | US-15 |
| RF-18 | Arquivar e reativar projeto (Gestor ou administrador); um projeto arquivado fica só de leitura. *(Extra, depois da Ficha 03.)* | S | Não perder histórico. ✅ P-10 | US-16 |
| RF-19 | Registar cada promoção e despromoção de administrador (quem fez, a quem, quando); os administradores podem consultar este registo. | S | É uma ação sensível e deve deixar rasto (Ex. 6, R-15). | US-11 |
| RF-20 | Adicionar e listar comentários de uma tarefa; o autor e a data são definidos pelo servidor. Conteúdo com 1 a 2000 caracteres, não só espaços. | M | Âmbito da Ficha 03 (C-02). | US-17 |
| RF-21 | Eliminar projeto (só Gestor), após confirmação, eliminando também membros, tarefas, atribuições e comentários. | M | Âmbito da Ficha 03 (C-03). | US-18 |

## 5. Requisitos não funcionais

| ID | Categoria | Requisito | Prioridade |
|----|-----------|-----------|-----------|
| RNF-01 | Segurança | Passwords guardadas apenas com hash e salt, usando bcrypt ou Argon2 (ver `docs/constraints.md` §3). | M |
| RNF-02 | Segurança | Todas as operações, exceto registo e login, exigem sessão autenticada; a sessão expira após 8 horas de inatividade [P-28]. | M |
| RNF-03 | Autorização | As permissões (Membro, Gestor, Administrador) são verificadas no servidor em cada pedido. | M |
| RNF-04 | Segurança | Comunicação cifrada (HTTPS) em produção. | M |
| RNF-05 | Privacidade | Dados pessoais limitados a nome e email; cumprimento do RGPD [P-18]. | M |
| RNF-06 | Validação | Todos os dados de entrada são validados no servidor, com mensagens de erro em português. | M |
| RNF-07 | Usabilidade | Interface em português, utilizável em ecrãs com largura entre 768 px e 1920 px sem scroll horizontal [P-09, P-16]. | S |
| RNF-08 | Desempenho | A lista de tarefas de um projeto com 500 tarefas responde em menos de 1 segundo [P-12]. | S |
| RNF-09 | Escala | Suporta até ~50 utilizadores e ~100 projetos ativos [P-01]. | S |
| RNF-10 | Manutenibilidade | Código versionado em Git, com testes automáticos para as regras de negócio. | S |
| RNF-11 | Segurança | Após 5 tentativas de login falhadas seguidas para a mesma conta, o login dessa conta fica bloqueado durante 15 minutos [P-29]. | M |

## 6. Âmbito do MVP

O MVP inclui **RF-01 a RF-19** e permite este fluxo de ponta a ponta:

1. Na instalação, é criado o primeiro administrador global.
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

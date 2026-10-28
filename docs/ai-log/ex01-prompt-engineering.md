# Ficha 01 — Exercício 1: Prompt Engineering

> Ferramenta de IA usada: Claude (Claude Code). As duas respostas abaixo foram geradas em conversas sem contexto do projeto, como pede o enunciado. Estão resumidas.

## Parte A — Prompt simples

### Prompt

```text
Quero criar uma aplicação de gestão de projetos e tarefas chamada Matriz. Ajuda-me a desenvolver a aplicação.
```

### Resposta da IA (resumo)

> Ótima ideia! Aqui fica uma proposta para a Matriz:
>
> **Funcionalidades:** registo e login; criação de projetos; quadro Kanban com arrastar e largar; tarefas com título, descrição, prioridade, etiquetas, data limite e responsável; subtarefas; comentários e anexos; notificações em tempo real; dashboard com gráficos de progresso; papéis de administrador, gestor e membro.
>
> **Stack sugerida:** React + TypeScript no frontend, Node.js com Express (ou NestJS) no backend, PostgreSQL com Prisma, autenticação JWT, WebSockets (Socket.IO) para tempo real, Docker para deploy.
>
> **Arquitetura:** SPA a comunicar com uma API REST; separação em camadas (controllers, services, repositories).
>
> **Próximos passos:** 1) criar o repositório e a estrutura de pastas; 2) definir o schema da base de dados; 3) implementar a autenticação…
>
> Aqui está o schema Prisma inicial:
> ```prisma
> model User    { id Int @id @default(autoincrement()) email String @unique ... }
> model Project { ... }
> model Task    { ... status Status @default(TODO) ... }
> ```
> Queres que avance com o código do backend?

### Análise

| Pergunta do enunciado | Observação |
|-----------------------|------------|
| **Que funcionalidades foram sugeridas?** | Autenticação, projetos, Kanban com drag & drop, tarefas com muitos campos, subtarefas, comentários, anexos, notificações em tempo real, dashboard com gráficos e três papéis. É uma lista genérica, "à Jira", muito maior do que o necessário para um primeiro produto. |
| **Que decisões assumiu sem confirmação?** | O tipo de aplicação (web SPA); os papéis (admin/gestor/membro); o fluxo de estados (TODO/…); que é preciso tempo real; que há anexos (implica armazenamento de ficheiros); o modelo de dados completo; JWT como autenticação; Docker como deploy. Nada disto vem da descrição do cliente. |
| **Propôs arquitetura ou tecnologias?** | Sim: React, Node/Express ou NestJS, PostgreSQL, Prisma, Socket.IO, Docker, arquitetura em camadas. E passou logo para código (schema Prisma), sem requisitos. |
| **O que devia ter sido clarificado primeiro?** | Quem são os utilizadores e quantos; o que significa "acompanhar o progresso"; que papéis e permissões existem; que estados tem uma tarefa; se é preciso colaboração em tempo real, notificações ou anexos; restrições de prazo, orçamento, tecnologia ou alojamento; requisitos de segurança/privacidade (RGPD); o que é o mínimo para a primeira versão. |

**Conclusão da Parte A:** um prompt vago leva a IA a "preencher os vazios" com o caso mais comum. A resposta parece completa e convincente, mas mistura factos (o nome Matriz e a ideia geral) com dezenas de suposições que ninguém aprovou, e salta diretamente para tecnologias e código.

---

## Parte B — Prompt estruturado

### Prompt (Goal → Context → Constraints → Task → Validation)

```text
# Goal
Quero transformar a descrição inicial de um cliente numa lista clara do que já sabemos
e do que falta saber, para preparar a especificação de requisitos.

# Context
O cliente quer uma aplicação chamada Matriz. A única informação que deu foi:
"Precisamos de uma aplicação onde as nossas equipas possam organizar projetos,
gerir tarefas e acompanhar o progresso do trabalho."
Ainda não há funcionalidades, regras de negócio, requisitos técnicos nem prioridades
definidos. Esta é a fase de análise; não há código.

# Constraints
- Atua como analista de requisitos experiente.
- NÃO proponhas tecnologias, frameworks, bases de dados nem arquitetura.
- NÃO escrevas código.
- Separa claramente FACTOS (o que está escrito na descrição) de PRESSUPOSTOS
  (o que estás a inferir). Não apresentes pressupostos como factos.
- Responde em português de Portugal.

# Task
1. Analisa a descrição do cliente.
2. Lista os factos conhecidos.
3. Lista os pressupostos que seria tentador assumir, e porquê são arriscados.
4. Identifica a informação em falta, agrupada por tema (utilizadores, funcionalidades,
   regras de negócio, requisitos não funcionais, restrições do projeto).
5. Formula perguntas de clarificação para o cliente, numeradas, por ordem de
   importância para definir um MVP.

# Validation
Antes de responder, confirma que:
- nenhuma tecnologia ou linha de código aparece na resposta;
- cada facto pode ser apontado na frase do cliente;
- cada pergunta é concreta e tem uma resposta possível do cliente (não é genérica).
```

### Resposta da IA (resumo)

**Factos conhecidos (da descrição do cliente)**
1. O nome da aplicação é Matriz.
2. Os utilizadores são "equipas" do cliente (há mais do que uma).
3. As equipas organizam **projetos**.
4. As equipas gerem **tarefas**.
5. É preciso **acompanhar o progresso** do trabalho.

**Pressupostos tentadores (e o risco)**
- *As tarefas pertencem a projetos.* Provável, mas a frase não o diz; podem existir tarefas soltas.
- *"Progresso" = % de tarefas concluídas.* Pode ser tempo gasto, prazos cumpridos ou marcos.
- *É uma aplicação web.* Pode ser preciso móvel ou desktop.
- *Há papéis diferentes (gestor/membro).* Não é dito; pode bastar todos iguais.
- *Os utilizadores são internos.* Podem existir clientes ou externos com acesso.
- *É parecida com o Jira/Trello.* A comparação é útil, mas não diz que funcionalidades desses produtos são desejadas.

**Informação em falta**
- *Utilizadores:* quantas pessoas e equipas; que papéis e permissões; se há utilizadores externos.
- *Funcionalidades:* que dados tem uma tarefa; que estados; se há atribuição a pessoas; comentários, anexos, notificações; vistas (lista, quadro, calendário).
- *Regras de negócio:* quem pode criar/apagar projetos e tarefas; como se mede o progresso; o que acontece a um projeto terminado.
- *Não funcionais:* dispositivos; idioma; volume de dados; disponibilidade; segurança e privacidade (RGPD).
- *Restrições:* prazo para a primeira versão; orçamento; sistemas existentes a integrar; preferências de alojamento.

**Perguntas de clarificação (por prioridade para o MVP)**
1. Quem vai usar a Matriz: quantas pessoas e equipas, e são todas internas?
2. Todos os utilizadores podem fazer tudo, ou há papéis com permissões diferentes? Quais?
3. Uma tarefa pertence sempre a um projeto? Que informação deve ter (responsável, prazo, prioridade…)?
4. Que estados pode ter uma tarefa, e o fluxo é igual para todos os projetos?
5. O que significa "acompanhar o progresso" para vocês? Que informação querem ver e quem a vê?
6. Uma tarefa pode ter mais do que um responsável?
7. Cada pessoa deve ver só os projetos onde participa, ou todos?
8. Que funcionalidades são indispensáveis no primeiro dia e quais podem esperar (ex.: comentários, anexos, notificações)?
9. Em que dispositivos vai ser usada (computador, telemóvel)?
10. Há prazo ou orçamento para a primeira versão?
11. Há dados pessoais ou confidenciais nas tarefas? Que requisitos legais ou de segurança se aplicam?
12. É preciso integrar com algum sistema existente (email, calendário, autenticação da empresa)?

---

## Comparação crítica

| Critério | Prompt simples (A) | Prompt estruturado (B) |
|----------|-------------------|------------------------|
| Papel da IA | Programador/consultor genérico que "resolve tudo". | Analista de requisitos. |
| Factos vs. pressupostos | Misturados; os pressupostos parecem decisões. | Separados de forma explícita. |
| Tecnologia e código | Propõe a stack e escreve código. | Nenhum (restrição respeitada). |
| Âmbito | Lista enorme, sem prioridades nem MVP. | Pergunta o que é indispensável. |
| Utilidade para a fase atual | Baixa: teria de ser desfeita depois. | Alta: dá a base para o Exercício 2. |
| Verificabilidade | Difícil de validar. | O bloco *Validation* dá critérios para conferir a resposta. |

**Vantagens do prompt estruturado**
- **Goal** e **Task** dão um resultado concreto e com formato previsível.
- **Context** limita a IA à informação real, em vez do "caso típico".
- **Constraints** impedem que a IA tome decisões fora de fase (tecnologia, código).
- **Validation** obriga a IA a rever a resposta e dá-nos critérios objetivos para a avaliar.
- A separação entre factos e pressupostos torna visível o que tem de ser confirmado com o cliente.

**Limitações observadas (mesmo no prompt B)**
- A IA continua a não saber as respostas; só as perguntas são fiáveis. As respostas serão pressupostos nossos (Exercício 2).
- Algumas perguntas sobrepõem-se (ex.: 2 e 7); o humano tem de as rever e agrupar.
- A IA pode ainda trazer vieses de produtos conhecidos (Jira/Trello) nas perguntas que faz.

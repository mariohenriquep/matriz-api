# Matriz

Aplicação de gestão de projetos e tarefas para equipas de trabalho, desenvolvida no âmbito da UC *AI-Powered Software Engineering*.

## O problema

As equipas precisam de um sítio único onde organizar os seus projetos, distribuir tarefas e saber em que ponto está o trabalho. Sem isso, as tarefas perdem-se em emails e conversas, não é claro quem faz o quê, e é difícil perceber quanto falta para terminar um projeto.

Descrição do cliente:
> "Precisamos de uma aplicação onde as nossas equipas possam organizar projetos, gerir tarefas e acompanhar o progresso do trabalho."

## Objetivos

- Organizar o trabalho em projetos com membros.
- Criar, atribuir e atualizar tarefas.
- Acompanhar o progresso de cada projeto.
- Garantir que cada pessoa só vê os projetos que lhe dizem respeito.

## Funcionalidades previstas para o MVP

- Registo e início de sessão; a primeira conta é administrador global.
- Criação de projetos por qualquer utilizador, que fica Gestor do projeto.
- Gestão de membros de cada projeto.
- Tarefas com título, descrição, prioridade, responsável e data limite.
- Três estados: `Por fazer`, `Em curso`, `Concluída`.
- Lista de tarefas com filtros e quadro Kanban.
- Painel de progresso por projeto.
- Notificações dentro da aplicação.
- "As minhas tarefas".
- Arquivo e reativação de projetos.
- Administração global: promover e despromover administradores.

Detalhe em [`docs/requirements.md`](docs/requirements.md) e [`docs/user-stories.md`](docs/user-stories.md).

## Organização da documentação

| Ficheiro | Conteúdo |
|----------|----------|
| [`README.md`](README.md) | Apresentação do projeto (este ficheiro). |
| [`AGENTS.md`](AGENTS.md) | Instruções para agentes de IA que trabalhem neste repositório. |
| [`docs/requirements.md`](docs/requirements.md) | Visão, utilizadores, requisitos funcionais e não funcionais, âmbito do MVP. |
| [`docs/user-stories.md`](docs/user-stories.md) | Backlog do MVP: user stories, prioridades, critérios de aceitação e casos-limite. |
| [`docs/constraints.md`](docs/constraints.md) | Restrições, pressupostos, segurança e privacidade, decisões técnicas pendentes. |
| [`docs/ai-log/`](docs/ai-log/) | Registo do uso de IA em cada exercício (prompts, análise e revisão humana). |

## Estado atual

**Fase 1 — Especificação (Ficha PL 01).** Requisitos, backlog e restrições definidos. Ainda não há código nem decisões de tecnologia; a arquitetura, o modelo de dados e a API são tratados na fase seguinte (Ficha PL 02).

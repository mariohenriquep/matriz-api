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
- Garantir que cada pessoa só vê os projetos que lhe dizem respeito *(pressuposto P-14, por confirmar)*.

## Funcionalidades previstas para o MVP

- Registo e início de sessão; o primeiro administrador global é criado na instalação.
- Criação de projetos por qualquer utilizador, que fica Gestor do projeto.
- Gestão de membros de cada projeto.
- Tarefas com título, descrição, prioridade, responsável e data limite; as que passam do prazo aparecem como "Atrasada".
- Três estados: `Por fazer`, `Em curso`, `Concluída`.
- Lista de tarefas com filtros e quadro Kanban.
- Painel de progresso por projeto.
- Notificações dentro da aplicação.
- "As minhas tarefas".
- Arquivo e reativação de projetos.
- Administração global: promover e despromover administradores, com registo dessas ações.

Detalhe em [`docs/requirements.md`](docs/requirements.md) e [`docs/user-stories.md`](docs/user-stories.md).

## Organização da documentação

| Ficheiro | Conteúdo |
|----------|----------|
| [`README.md`](README.md) | Apresentação do projeto (este ficheiro). |
| [`AGENTS.md`](AGENTS.md) | Instruções para agentes de IA que trabalhem neste repositório. |
| [`docs/requirements.md`](docs/requirements.md) | Visão, utilizadores, requisitos funcionais e não funcionais, âmbito do MVP. |
| [`docs/user-stories.md`](docs/user-stories.md) | Backlog do MVP: user stories, prioridades, critérios de aceitação e casos-limite. |
| [`docs/constraints.md`](docs/constraints.md) | Restrições, pressupostos, segurança e privacidade, decisões técnicas pendentes. |
| [`docs/architecture.md`](docs/architecture.md) | Arquitetura (monólito modular), componentes e decisões transversais. |
| [`docs/database.md`](docs/database.md) | Modelo de dados, diagrama ER (Mermaid) e regras de negócio. |
| [`openapi.yaml`](openapi.yaml) | Contrato REST (OpenAPI 3.1) das operações de tarefas e lista dos endpoints previstos. |
| [`docs/ui/`](docs/ui/) | Wireframe do painel do projeto (`wireframe.png`, fonte em `wireframe.html`). |
| [`docs/ai-log/`](docs/ai-log/) | Registo do uso de IA em cada exercício (prompts, análise e revisão humana). |

## Estado atual

- ✅ **Fase 1 — Especificação (Ficha PL 01):** requisitos, backlog e restrições.
- ✅ **Fase 2 — Conceção (Ficha PL 02):** arquitetura, modelo de dados, contrato da API e wireframe.
- ⏭️ **Fase 3 — Desenvolvimento (Aula 03):** escolha da stack (D-01, D-02, D-05) e implementação.

Ainda não há código.

# Arquitetura — Matriz

> Ficha 02, Exercício 1. Baseado em `docs/requirements.md`, `docs/user-stories.md` e `docs/constraints.md`.
> **[P-xx]** = pressuposto; **D-xx** = decisão técnica (ver `docs/constraints.md` §4).

## 1. Fatores que pesam na decisão

| Fator | Valor | Fonte |
|-------|-------|-------|
| Escala | Até ~50 utilizadores e ~100 projetos ativos. | P-01, RNF-09 |
| Equipa | Um estudante, em contexto de aulas, com prazos curtos. | R-01, P-17 |
| Domínio | Um único domínio coeso: projetos, membros, tarefas, notificações. As regras cruzam-se muito (ex.: o responsável tem de ser membro; um projeto arquivado bloqueia as tarefas). | RF-07 a RF-18 |
| Tempo real | Não exigido. As notificações são in-app e podem ser consultadas periodicamente. | RF-16, P-21 |
| Disponibilidade | Sem requisitos de alta disponibilidade no MVP. | constraints.md §5 |
| Evolução | Funcionalidades futuras já identificadas (comentários, email, relatórios). | requirements.md §7 |

## 2. Comparação das alternativas

| Critério | Monólito | Monólito modular | Microservices |
|----------|----------|------------------|---------------|
| Complexidade de desenvolvimento | Baixa | Baixa a média | Alta (vários serviços, comunicação em rede, contratos entre serviços) |
| Deploy e operação | Uma aplicação | Uma aplicação | Vários serviços, orquestração, observabilidade distribuída |
| Consistência dos dados | Transações simples numa só base de dados | Igual ao monólito | Difícil: regras que cruzam serviços exigem coordenação (ex.: remover membro → tarefas sem responsável) |
| Organização do código | Tende a misturar responsabilidades à medida que cresce | Fronteiras claras entre módulos | Fronteiras fortes (por serviço) |
| Escala independente | Não | Não | Sim, mas não é necessária com ~50 utilizadores |
| Preparação para o futuro | Fraca | Boa: um módulo pode ser extraído para serviço se for preciso | Já está "pronto", mas paga-se o custo desde o início |
| Adequação ao MVP | Aceitável | **Boa** | Fraca: custo muito superior ao benefício |

## 3. Decisão

**Monólito modular**, com uma aplicação web (SPA) separada que consome uma API REST. ✅ Aprovado pelo Mário (D-04).

**Justificação**

1. A escala do MVP (P-01) não justifica microservices: não há partes do sistema que precisem de escalar de forma independente.
2. As regras de negócio cruzam entidades (membros ↔ tarefas ↔ notificações) e precisam de transações simples numa só base de dados.
3. Uma só aplicação para desenvolver, testar e publicar é adequada a uma equipa de uma pessoa e ao calendário das aulas.
4. Face ao monólito simples, os módulos com fronteiras explícitas mantêm o código organizado, facilitam o trabalho com agentes de IA (uma tarefa = um módulo) e permitem extrair um módulo mais tarde, se for preciso.

**Consequências aceites**

- A aplicação escala só como um todo (suficiente para RNF-09).
- É preciso disciplina para respeitar as fronteiras entre módulos: um módulo só usa outro através da sua interface pública, nunca acedendo diretamente às suas tabelas.
- Dentro do backend, o código segue a clean architecture descrita na §6.

## 4. Componentes

```mermaid
flowchart LR
    U[Utilizador<br/>browser] --> FE[Frontend SPA]
    FE -- HTTPS / REST + JSON --> API

    subgraph API[Backend: monólito modular]
        direction TB
        IDN[Identidade e acesso]
        PRJ[Projetos e membros]
        TAR[Tarefas]
        PRG[Progresso]
        NOT[Notificações]
        ADM[Administração]
    end

    API --> DB[(Base de dados<br/>relacional)]
```

| Componente | Responsabilidades | Requisitos |
|------------|-------------------|-----------|
| **Frontend (SPA)** | Ecrãs (login, projetos, lista e quadro Kanban, painel de progresso, notificações); validação básica de formulários; chama a API. Não contém regras de autorização. | RNF-07 |
| **API REST** | Ponto de entrada único; autenticação de cada pedido; tradução HTTP ↔ módulos; erros em formato uniforme. | RNF-02, RNF-06 |
| Módulo **Identidade e acesso** | Registo, login, logout, sessões, hash de passwords, bloqueio após tentativas falhadas; verificação de papéis (Membro, Gestor, Administrador). | RF-01, RNF-01, RNF-02, RNF-03, RNF-11 |
| Módulo **Projetos e membros** | Criar, editar, arquivar e reativar projetos; adicionar e remover membros; regras do Gestor. | RF-03 a RF-07, RF-18 |
| Módulo **Tarefas** | Criar, editar, eliminar e atribuir tarefas; estados; filtros; "As minhas tarefas"; marcação "Atrasada"; bloqueio de escrita em projetos arquivados. | RF-08 a RF-12, RF-15, RF-17 |
| Módulo **Progresso** | Contagens por estado e % concluída, calculadas a partir das tarefas (sem dados próprios). | RF-14 |
| Módulo **Notificações** | Recebe eventos dos outros módulos (tarefa atribuída, membro adicionado, estado alterado), cria notificações, gere lidas/não lidas. | RF-16 |
| Módulo **Administração** | Primeiro administrador na instalação; promover e despromover; registo dessas ações. | RF-02, RF-13, RF-19 |
| **Base de dados relacional** | Guarda utilizadores, projetos, membros, tarefas, notificações e registo de administração. | — |

**Regras de dependência entre módulos**

- Todos os módulos usam **Identidade e acesso** para saber quem é o utilizador e que papel tem.
- **Tarefas** usa **Projetos e membros** para confirmar que o projeto está ativo e que o responsável é membro.
- **Progresso** só lê de **Tarefas**.
- **Projetos e membros**, **Tarefas** e **Administração** publicam eventos; **Notificações** só os consome (não chama os outros módulos). Assim, acrescentar notificações por email no futuro não mexe nos outros módulos.

## 5. Decisões transversais

| Tema | Decisão | Estado |
|------|---------|--------|
| Estilo de API | REST com JSON; contrato em `openapi.yaml` (Exercício 3). | Proposto |
| Erros | Formato uniforme baseado em *Problem Details* (RFC 9457), com mensagens em português. | Proposto |
| Entrega de notificações (D-07) | O frontend consulta o contador de não lidas periodicamente (ex.: a cada 30 s) e ao mudar de ecrã. Sem WebSockets no MVP. | ✅ Decidido |
| Base de dados (D-03) | Relacional, pela natureza das relações e regras de integridade. Motor concreto por decidir. | ✅ Decidido (motor pendente) |
| Autenticação (D-05) | JWT em `Authorization: Bearer`, válido 8 h; passwords com BCrypt. | ✅ Decidido (Ficha 03) |
| Linguagem e frameworks (D-01, D-02) | Backend: Java 21 + Spring Boot 3 (Web, Security, Data JPA, Validation, Flyway). Frontend: React + Vite + TypeScript. Base de dados: PostgreSQL 16. Detalhe em `docs/implementation-plan.md` §2. | ✅ Decidido (Ficha 03) |

## 6. Organização do código no backend: clean architecture ✅

> Extra pedido pelo Mário a 2026-10-29, antes do Exercício 5 da Ficha 03 (não faz parte do enunciado).

Os módulos da §4 continuam a ser a divisão funcional. Dentro do backend, o código está organizado por **camadas**, cada uma num pacote de `pt.matriz`. Cada módulo tem as suas classes em várias camadas (por exemplo, Projetos e membros = `ControladorProjetos`, `ServicoProjetos`, `ProjetoRepositorio`, `ProjetoMapper` e os DTOs de projetos).

```mermaid
flowchart LR
    HTTP([Pedido HTTP]) --> C[controllers]
    C --> M[mappers]
    M --> D2[dtos]
    C --> D2
    C --> S[services]
    S --> R[repositories]
    S --> DOM[domain]
    R --> DOM
    M --> DOM
    R --> BD[(PostgreSQL)]
```

| Pacote | Responsabilidade | Exemplos |
|--------|------------------|----------|
| `domain` | Entidades, valores e enums do negócio. Não conhece HTTP, DTOs nem serviços. | `Utilizador`, `Projeto`, `Membro`, `Papel`, `ContagensTarefas`, `EdicaoProjeto` |
| `repositories` | Acesso à base de dados (SQL explícito com `NamedParameterJdbcTemplate`; Spring Data JPA só para `Utilizador`). Devolve objetos do domínio. | `ProjetoRepositorio`, `UtilizadorRepositorio` |
| `services` | Casos de uso e regras de negócio: validações, regras de acesso, transações. Recebem e devolvem objetos do domínio. | `ServicoProjetos`, `ServicoAutenticacao`, `ControloAcesso` |
| `controllers` | Só HTTP: rotas, códigos de estado, validação do formato do pedido. Recebem DTOs, chamam serviços e devolvem DTOs. | `ControladorProjetos`, `ControladorAutenticacao` |
| `dtos` | Formato dos pedidos e respostas, igual aos schemas do `openapi.yaml`. Só dados. | `NovoProjeto`, `AlteracaoProjeto`, `ProjetoResposta`, `Lista` |
| `mappers` | Conversão entre domínio e DTOs, nos dois sentidos. | `ProjetoMapper`, `UtilizadorMapper` |
| `security` | Spring Security, emissão e validação de JWT, utilizador autenticado. | `ConfiguracaoSeguranca`, `ServicoTokens`, `UtilizadorAtual` |
| `exceptions` | Erros da API e conversão para Problem Details. | `ErroApi`, `TratamentoErros` |
| `config` | Configuração geral e dados de demonstração. | `ConfiguracaoComum`, `DadosDemonstracao` |

**Regra de dependências.** As dependências só apontam para dentro: `controllers → services → repositories → domain`. Em concreto:

- `domain` não depende de nenhuma outra camada da aplicação;
- `controllers` nunca usam `repositories` diretamente;
- `services` não conhecem `controllers`, `dtos` nem `mappers` (não sabem que existe HTTP);
- `repositories` só conhecem o `domain`;
- `dtos` são só dados e `mappers` não acedem a serviços nem à base de dados.

A regra é verificada automaticamente pelo teste `ArquiteturaTest` (ArchUnit), que corre com `./mvnw test`: uma dependência proibida faz falhar o build.

**Compromissos aceites**

- `Utilizador` continua a ser uma entidade JPA dentro de `domain` (decisão do Mário). O domínio fica assim ligado às anotações `jakarta.persistence`, mas não ao Spring.
- `ErroApi` (em `exceptions`) é usado pelos serviços para indicar 404, 403, 409 ou 422. O código HTTP fica decidido no serviço; o controlador não traduz erros.
- Não há interfaces separadas para os repositórios ("portas"): com uma só implementação por repositório, seriam código a mais sem benefício nesta fase.

## 7. Pressupostos e pendentes desta fase

- A consulta periódica das notificações (30 s) é suficiente para os utilizadores [P-30, por confirmar].
- O frontend e o backend podem ser publicados no mesmo domínio, o que simplifica a autenticação [P-31, por confirmar].
- D-01, D-02, D-03 e D-05 foram fechadas na Ficha 03 (Exercício 1).

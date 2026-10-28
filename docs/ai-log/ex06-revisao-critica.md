# Ficha 01 — Exercício 6: Revisão crítica e Human-in-the-Loop

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).
> Documentos revistos: `README.md`, `AGENTS.md`, `docs/requirements.md`, `docs/user-stories.md`, `docs/constraints.md`.

## Prompt

```text
# Goal
Encontrar problemas na documentação da Matriz antes de passarmos à arquitetura.

# Context
Em anexo: README.md, AGENTS.md, docs/requirements.md, docs/user-stories.md e
docs/constraints.md. As decisões marcadas com ✅ foram confirmadas pelo cliente.

# Constraints
- Atua como revisor externo exigente (analista de requisitos + especialista em segurança).
- Não reescrevas os documentos; lista apenas problemas, com a localização exata
  (ficheiro e ID) e uma sugestão concreta de correção.
- Não proponhas funcionalidades novas, a não ser para corrigir um risco.
- Não ponhas em causa decisões ✅ sem um motivo concreto.
- Português de Portugal.

# Task
Identifica:
1. requisitos ambíguos ou contraditórios;
2. funcionalidades não justificadas;
3. pressupostos apresentados como factos;
4. critérios de aceitação não verificáveis;
5. riscos de segurança ou privacidade;
6. inconsistências entre documentos.

# Validation
- Cada problema tem ficheiro e ID.
- Cada sugestão é aplicável sem informação nova do cliente, ou diz que precisa dela.
```

## Resposta da IA e decisão

Legenda da decisão: **Aceitar** (aplicar a sugestão tal como está), **Alterar** (aplicar uma versão diferente), **Rejeitar** (não aplicar). A coluna "Proposta" é a minha recomendação; a decisão final é do Mário.

### 1. Requisitos ambíguos ou contraditórios

| # | Onde | Problema | Sugestão da IA | Proposta | Porquê |
|---|------|----------|----------------|----------|--------|
| R-01 | US-16 crit. 3; RF-18 | Não diz **quem** pode reativar um projeto arquivado. | Indicar "Gestor ou administrador global". | **Aceitar** | É coerente com quem pode arquivar (crit. 1). |
| R-02 | US-05; RF-07 | Não diz se o **administrador** pode remover o Gestor de um projeto. Sem isso, um projeto cujo Gestor sai da empresa fica sem gestão. | Permitir ao administrador remover o Gestor e transferir a gestão para outro membro. | **Alterar** | Transferir a gestão é uma funcionalidade nova. Proposta mais simples: o administrador já pode gerir qualquer projeto (P-02), por isso basta escrever que o Gestor não pode ser removido e que o administrador assume a gestão quando é preciso. |
| R-03 | US-08; RF-10 | O administrador "gere todos os projetos", mas o responsável "tem de ser membro". Não está claro se o administrador pode ser responsável de tarefas de um projeto onde não é membro. | Clarificar que o administrador só pode ser responsável se for membro do projeto. | **Aceitar** | Mantém a regra simples e já confirmada do RF-10. |
| R-04 | US-03 crit. 2; RF-05 | US-03 diz que o administrador vê "todos os projetos **ativos**"; RF-05 diz "todos". Não é claro se vê os arquivados. | Uniformizar: vê todos os ativos na lista principal e todos os arquivados em "Projetos arquivados". | **Aceitar** | Clarifica sem mudar nada. |

### 2. Funcionalidades não justificadas

| # | Onde | Problema | Sugestão da IA | Proposta | Porquê |
|---|------|----------|----------------|----------|--------|
| R-05 | US-16 crit. 3 | A reativação de projetos foi acrescentada pela IA no Exercício 3; não vem do cliente. | Remover. | **Rejeitar** | Foi aceite explicitamente pelo Mário no Exercício 3 e é barata de implementar. Fica justificada no RF-18. |
| R-06 | RF-17 "As minhas tarefas" | A justificação ("foco individual") é fraca e o filtro por responsável (RF-12) já dá algo parecido, mas só por projeto. | Manter, com a justificação "ver o trabalho de todos os projetos num só sítio, o que o RF-12 não permite". | **Aceitar** | A diferença em relação ao RF-12 passa a ficar explícita. |

### 3. Pressupostos apresentados como factos

| # | Onde | Problema | Sugestão da IA | Proposta | Porquê |
|---|------|----------|----------------|----------|--------|
| R-07 | README.md, "Funcionalidades" e "Objetivos" | Diz "a primeira conta é administrador global" e "cada pessoa só vê os projetos que lhe dizem respeito" como factos; são P-20 e P-14, por confirmar. | Assinalar com a etiqueta do pressuposto, como nos outros ficheiros. | **Aceitar** | O README é o primeiro ficheiro lido; não pode esconder pressupostos. |
| R-08 | requirements.md §1 | "Único requisito confirmado pelo cliente" convive com ✅ "confirmado pelo Mário (atuando como cliente)". Pode parecer contraditório. | Explicar no topo que, neste trabalho, o Mário faz o papel de cliente. | **Aceitar** | Evita confusão para quem lê. |

### 4. Critérios de aceitação ou requisitos não verificáveis

| # | Onde | Problema | Sugestão da IA | Proposta | Porquê |
|---|------|----------|----------------|----------|--------|
| R-09 | RNF-02 | "As sessões expiram" não diz quando. | Definir: expiram após 8 horas de inatividade. | **Alterar** | O valor depende do mecanismo de autenticação (D-05), ainda pendente. Proposta: registar como pressuposto P-28 ("sessão expira após 8 h de inatividade, a rever com D-05"). |
| R-10 | RNF-07 | "Utilizável em computador e tablet" não é verificável. | Definir larguras de ecrã mínimas (ex.: 768 px a 1920 px) e "sem scroll horizontal". | **Aceitar** | Passa a ser testável. |
| R-11 | RNF-01 | "Hash forte" não é verificável por si só. | Remeter para os algoritmos listados em constraints.md §3 (bcrypt ou Argon2). | **Aceitar** | Já está decidido em constraints.md; só falta a ligação. |

### 5. Riscos de segurança e privacidade

| # | Onde | Problema | Sugestão da IA | Proposta | Porquê |
|---|------|----------|----------------|----------|--------|
| R-12 | RF-02; P-20 | **Tomada de controlo no arranque:** se a Matriz ficar acessível na internet antes de a pessoa certa se registar, qualquer pessoa pode registar-se primeiro e ficar administradora. | Criar o primeiro administrador por configuração na instalação, e não pelo primeiro registo. | **Aceitar** | Risco real e fácil de evitar. Precisa da tua decisão porque muda o P-20. |
| R-13 | US-01; RF-01 | **Registo aberto:** qualquer pessoa na internet pode criar conta e projetos, mas P-01 diz que os utilizadores são equipas internas. | Restringir o registo a emails de um domínio da empresa, ou só por convite do administrador. | **Rejeitar (por agora)** | Ambas as opções são funcionalidades novas e o cliente não definiu domínios. Registar como risco conhecido em constraints.md e decisão pendente D-09. |
| R-14 | US-04 crit. 3 | **Enumeração de contas:** a mensagem "Não existe utilizador com este email" deixa um Gestor descobrir que emails têm conta, o que contraria a lógica da mensagem genérica no login. | Mensagem genérica também aqui. | **Rejeitar** | Quem adiciona membros já está autenticado e precisa de saber porque é que a operação falhou. Num sistema interno, o risco é baixo. Fica registado como risco aceite. |
| R-15 | constraints.md §3 | Diz que promover a administrador "deve ficar registada (quem, quando)", mas não há RF para isso e o histórico de alterações está fora do âmbito. | Criar um RF para o registo de promoções/despromoções. | **Aceitar** | É pequeno, e é uma ação sensível que deve deixar rasto. |

### 6. Inconsistências entre documentos

| # | Onde | Problema | Sugestão da IA | Proposta | Porquê |
|---|------|----------|----------------|----------|--------|
| R-16 | CE-06 e P-27 vs. requirements.md | A marcação "Atrasada" está no backlog e nos pressupostos (✅), mas não há RF nem está no README. | Incluir no RF-08 (criar tarefa) ou num RF próprio. | **Aceitar** | Acrescentar ao RF-08: "tarefas com data limite ultrapassada e não concluídas aparecem como Atrasada". |
| R-17 | constraints.md §3 vs. requirements.md §5 | "Limitar tentativas de login" está nas regras de segurança mas não nos RNF. | Acrescentar RNF: bloquear temporariamente após 5 tentativas falhadas seguidas. | **Alterar** | Acrescentar o RNF, mas o número exato fica como pressuposto (P-29) para não parecer decisão do cliente. |
| R-18 | AGENTS.md §6 | Exige aprovação para alterar `docs/requirements.md`, `docs/user-stories.md` e `docs/constraints.md`, mas não menciona `README.md` nem `AGENTS.md`. | Incluir os dois. | **Aceitar** | O próprio AGENTS.md é contexto crítico. |

## Resumo da proposta

- **Aceitar:** 12 (R-01, 03, 04, 06, 07, 08, 10, 11, 12, 15, 16, 18).
- **Alterar:** 3 (R-02, 09, 17).
- **Rejeitar:** 3 (R-05, 13, 14).

## Decisão do Mário

*(por preencher depois da revisão)*

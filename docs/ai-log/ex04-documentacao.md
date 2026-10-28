# Ficha 01 — Exercício 4: Context Engineering e Documentação

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Criar uma base de conhecimento persistente da Matriz, para ser usada como contexto por
pessoas e agentes de IA nas próximas aulas.

# Context
Em anexo: análise e decisões do Exercício 2, backlog do Exercício 3 (docs/user-stories.md)
e a lista de pressupostos P-01 a P-26 com o estado de cada um.

# Constraints
- Não inventes requisitos novos; usa só o que está nos anexos.
- Distingue sempre o que foi confirmado (✅) do que é pressuposto.
- Sem tecnologias escolhidas: ficam como decisões pendentes.
- Cada informação vive num só ficheiro; os outros fazem referência a ele.
- Português de Portugal.

# Task
Organiza a informação em:
- docs/requirements.md: visão, objetivos, utilizadores, RF, RNF, MVP, fora do âmbito;
- docs/constraints.md: restrições, pressupostos, segurança e privacidade, decisões
  técnicas pendentes, limitações de âmbito;
- README.md: apresentação, problema, objetivos, MVP, organização dos docs, estado atual.
(docs/user-stories.md já existe do Exercício 3; só ajustar referências.)

# Validation
- Cada RF liga-se a pelo menos uma User Story e cada User Story a um RF.
- Cada pressuposto citado num ficheiro existe em docs/constraints.md.
- O que está fora do âmbito não aparece em nenhum RF nem User Story.
```

## Ficheiros produzidos

| Ficheiro | Conteúdo |
|----------|----------|
| [`docs/requirements.md`](../requirements.md) | Visão, 4 objetivos, 4 perfis, RF-01 a RF-18, RNF-01 a RNF-10, MVP, 13 itens fora do âmbito. |
| [`docs/user-stories.md`](../user-stories.md) | 16 User Stories (Exercício 3), com referências a pressupostos acertadas. |
| [`docs/constraints.md`](../constraints.md) | 5 restrições, 27 pressupostos (P-01 a P-27) com estado, segurança e privacidade, 8 decisões pendentes. |
| [`README.md`](../../README.md) | Apresentação do projeto e mapa da documentação. |

## Verificação de consistência

| Verificação | Resultado |
|-------------|-----------|
| Cada RF tem User Story (coluna "US" em requirements.md). | ✅ RF-01 a RF-18 cobrem US-01 a US-16; nenhuma US fica sem RF. |
| Cada pressuposto citado existe em constraints.md. | ✅ Citados: P-01, 02, 03, 04, 09, 10, 11, 12, 14, 16, 18, 20, 21, 22, 25, 27; todos definidos. |
| Nada do "fora do âmbito" aparece no backlog. | ✅ Comentários, anexos, email, SSO, etiquetas: nenhum em RF ou US. |
| Perfis iguais nos três ficheiros. | ✅ Visitante, Membro, Gestor, Administrador global. |
| Tecnologia não decidida. | ✅ Só em constraints.md §4 como pendente. |

## Correções feitas durante a consolidação

- Os números dos pressupostos do Exercício 2 tinham lacunas (P-09, P-11 e P-12 não existiam). Foram definidos em constraints.md: idioma, edição simultânea e desempenho.
- Dois casos-limite do backlog tinham só a etiqueta genérica **[P]**. Passaram a apontar para P-11 (edição simultânea) e para um pressuposto novo, **P-27** (tarefa com data limite no passado aparece como "Atrasada").
- As etiquetas de P-23, P-24 e P-25 foram retiradas das User Stories, porque já estão confirmadas.
- A remoção de membros e a promoção a administrador foram marcadas como ações sensíveis em constraints.md §3.
- Acrescentada a decisão pendente **D-07** (como entregar as notificações), que vai afetar a arquitetura na Ficha 02.

## Pendente

- O `README.md` já aponta para o `AGENTS.md`, que é criado no Exercício 5.

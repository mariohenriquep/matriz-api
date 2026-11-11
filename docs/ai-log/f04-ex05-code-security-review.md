# Ficha 04 — Exercício 5: Code Review e Security Review

> Ferramenta de IA usada: Claude (Claude Code), com o prompt de apoio da ficha, adaptado ao prompt estruturado (B).

## Prompt

```text
# Goal
Atua como Senior Software Engineer e Application Security Reviewer da Matriz.

# Context
Diff do PR #11 (último incremento da Ficha 03), ControloAcesso e serviços, ConfiguracaoSeguranca,
ServicoTokens, repositórios, contas de demonstração Ana / Rui / Joana, docs/requirements.md (RNF-01 a RNF-11).

# Constraints
- Procurar bugs relevantes, validações em falta, IDOR, exposição de dados e secrets; sem comentários só de estilo.
- Para cada finding: ficheiro, evidência, impacto, severidade e correção proposta.
- Distinguir factos observados (reproduzidos) de hipóteses; testar cada suspeita contra a aplicação.
- Não modificar código sem aprovação; sugerir um teste de regressão por correção.
- Registar o que foi executado e o que ficou por validar.

# Task
1. Code Review do diff. 2. IDOR com duas contas (e uma terceira para o caso Membro vs Gestor).
3. Semgrep, oxlint, npm audit. 4. Escrever docs/quality-security-review.md e pedir a decisão.
```

## O que a IA fez

- Leu o diff do PR #11 e todas as regras de acesso; levantou 4 suspeitas no código e 7 de segurança.
- Correu a aplicação sobre um seed novo e testou cada suspeita com pedidos reais (script em
  `scripts/verificar-idor.sh`, executado duas vezes). Resultado: 2 bugs de código confirmados (baixos),
  1 finding de segurança confirmado (médio: sem limite de tentativas de login), 1 de configuração,
  2 hipóteses/riscos aceites e 7 suspeitas refutadas.
- Semgrep a partir das regras públicas (o registo `semgrep.dev` está bloqueado neste ambiente): 86
  findings, todos de internacionalização (falsos positivos para uma interface só em português).

## Revisão humana das conclusões da IA

- Três suspeitas iniciais da IA foram refutadas pelos testes reais e não entram como findings: corrida ao
  atribuir o mesmo responsável (dava `409`, não `500`), password acima de 72 bytes (dava `401`) e
  enumeração de contas no login (respostas iguais).
- O CR-01 (contagem de caracteres) já tinha sido encontrado pelos testes unitários do Ex. 2 e corrigido.
- O Mário escolheu corrigir os 4 recomendados (SEC-01, CR-02, CR-03, SEC-02). Cada correção tem teste de
  regressão, e três deles foram executados também sem a correção, para confirmar que falham.
- Na revisão da correção do SEC-01, a IA acrescentou duas proteções que não estavam na primeira proposta:
  as colunas do bloqueio são só de leitura na entidade JPA (senão um logout podia repor um contador antigo)
  e a falha é registada com um `UPDATE` atómico (senão pedidos em paralelo podiam contar menos falhas).
- Durante a verificação na aplicação, o bloqueio do Rui fez falhar o `scripts/verificar-idor.sh`, que usa
  essa conta: era o comportamento esperado. As contas foram repostas e o script voltou a dar os mesmos resultados.

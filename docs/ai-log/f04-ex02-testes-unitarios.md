# Ficha 04 — Exercício 2: Testes unitários

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Testes unitários de uma regra de negócio implementada: a validação do título da tarefa.

# Context
docs/test-plan.md (T-01 a T-04), backend/src/main/java/pt/matriz/services/CampoTexto.java,
docs/requirements.md (RF-08), docs/constraints.md (título 1 a 200), JUnit 5 + AssertJ já no projeto.

# Constraints
- Caso válido, casos inválidos e valores-limite; os valores esperados vêm do requisito, não do código.
- Sem Spring nem base de dados.
- Se um teste falhar: distinguir defeito da aplicação, erro do teste e limitação do ambiente.
- Não alterar a regra para ficar verde; não mexer em código de produção sem autorização.

# Task
Escrever CampoTextoTest, executar, analisar as falhas e registar os resultados.

# Validation
./mvnw test -Dtest=CampoTextoTest; depois a suíte completa e os testes do frontend.
```

## Resultado

- 22 testes em `CampoTextoTest` (válido, espaços nas pontas, vazio/espaços/NBSP/BOM, 1, 200 e 201
  caracteres, limite depois da limpeza, letras acentuadas, 200 e 201 emojis, campo opcional).
- O limite do requisito está escrito no teste (200) e não lido do código, para o teste falhar se alguém
  mudar o limite sem mudar o requisito.
- 1.ª execução: 21 passam, 1 falha (200 emojis recusados). A análise (`docs/test-plan.md` §6.1) mostrou que
  era um defeito da aplicação, confirmado no PostgreSQL 16 e no Node: o código contava unidades UTF-16.

## Decisão humana

A IA propôs corrigir; o Mário aprovou a correção no backend e no frontend (cartão de decisão, 2026-11-11).
A correção mudou só a forma de contar (code points), não o limite nem as mensagens. Cada correção tem
teste de regressão; o teste novo do formulário foi executado também sem a correção e falhou, como esperado.

## O que ficou por rever

- Os testes iniciais passaram todos à primeira, exceto o dos emojis; nenhum teste teve de ser corrigido.
- Emojis compostos (sequências com ZWJ) continuam a contar como vários caracteres; ficou documentado como
  limitação, coerente com o PostgreSQL.

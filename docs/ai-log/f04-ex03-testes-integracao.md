# Ficha 04 — Exercício 3: Testes de integração

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Validar a API e a persistência real: criar uma tarefa válida e consultá-la depois pela API.

# Context
docs/test-plan.md (T-01, T-05, T-09), openapi.yaml (POST /projetos/{id}/tarefas, GET /tarefas/{id}),
base de testes TesteIntegracao/ApiTeste (MockMvc + PostgreSQL 16 em Testcontainers), decisão D-3.

# Constraints
- Respeitar o contrato real e as permissões: quem cria é membro; quem não é membro recebe 404.
- Verificar código HTTP, campos devolvidos e a linha gravada na base de dados.
- Sem @Transactional: commit real, dados próprios do teste e limpeza verificada no fim.
- Pelo menos um cenário negativo (título inválido e recurso inexistente).

# Task
Escrever CriarEConsultarTarefaTest, executar sozinho e com a suíte completa, duas vezes.

# Validation
./mvnw test -Dtest=CriarEConsultarTarefaTest; ./mvnw test (x2); prova de que a limpeza é necessária.
```

## Resultado

- 3 testes: fluxo criar → gravar → consultar (pelo id do `Location` e na lista do projeto, com contagens),
  título inválido (vazio, só espaços, 201 caracteres, ausente) sem nada gravado, e tarefa inexistente vs
  tarefa alheia com o mesmo `404`.
- Passaram à primeira; suíte completa com 100 testes verdes em duas execuções seguidas.
- Para provar que o isolamento não é só aparente, retirou-se temporariamente a limpeza: o `ProjetosTest`
  passou a falhar 3 testes, porque via projetos a mais. A limpeza foi reposta.

## Revisão humana das propostas da IA

- A primeira versão verificava o cabeçalho `Location` com uma expressão confusa e verificava também que o
  `GET` não tinha `Location`, o que não está em nenhum requisito. Foi simplificado para: o `Location` termina
  em `/api/v1/tarefas/{id}` da tarefa criada.
- O projeto do teste é criado pela API (e não por SQL) para respeitar o contrato e as regras de acesso; a
  limpeza é feita por SQL porque é preparação do teste, não comportamento em teste.

# Ficha 05 — Exercício 5: Documentação técnica e preparação da release

> Ferramenta de IA usada: Claude (Claude Code), com o prompt sugerido da ficha adaptado ao prompt estruturado (B).

## Prompt

```text
# Goal
Atualizar a documentação técnica da Matriz e preparar as notas da versão candidata 0.5.0.

# Context
README.md, docs/architecture.md, CHANGELOG.md, openapi.yaml, compose.yaml, Dockerfiles, ci.yml e os
resultados reais da CI do PR #19.

# Constraints
- Tudo o que se documenta tem de corresponder ao código existente; não inventar funcionalidades nem resultados.
- Identificar as informações não confirmadas e apresentá-las para revisão.

# Task
README (descrição, funcionalidades, tecnologias, pré-requisitos, instalação, execução, testes, referências),
diagrama Mermaid da execução, Docker Compose, OpenAPI, CHANGELOG e notas da versão candidata.
```

## Alterações

| Ficheiro | O que mudou |
|----------|-------------|
| `README.md` | Funcionalidades da 0.5.0 (o bloqueio do login da Ficha 04 passou de "pendente" a implementado); secção **Tecnologias**; Docker como pré-requisito suficiente; comandos e problemas comuns do Docker Compose; **Documentação da API** (como ler o `openapi.yaml`); **Integração contínua**; tabela da documentação; estado da Fase 5. |
| `docs/architecture.md` | §8 com dois diagramas Mermaid (containers e pipeline) e a tabela dos serviços. |
| `CHANGELOG.md` | Entrada `[0.5.0] - por lançar (versão candidata)`. |
| `docs/release-notes/v0.5.0.md` | Notas da versão candidata: novidades, segurança, alterações a ter em conta, verificação e limitações. |

## Verificações feitas pela IA

- Cada comando do README foi confrontado com o `compose.yaml`, o `ci.yml` e os `package.json`; os números de testes (107 e 32) vêm das execuções reais da CI.
- As tecnologias e versões vêm do `pom.xml`, dos `package.json` e dos Dockerfiles.
- O README dizia que o bloqueio após tentativas de login falhadas estava por fazer; está implementado desde a 0.4.0 (migration `V3__bloqueio_login.sql`, `BloqueioLoginTest`). Corrigido.

## Informações não confirmadas (para revisão do Mário)

1. A secção "Problemas comuns" do Docker Compose descreve causas prováveis, observadas no cloud e na CI; não foram reproduzidas uma a uma no Mac.
2. Os links para editores de OpenAPI (VS Code, IntelliJ, editor.swagger.io) não foram testados com o `openapi.yaml`.
3. A data da 0.5.0 fica por definir até ao Ex. 6.
4. A versão nos ficheiros (`pom.xml`, `package.json`, `openapi.yaml`) continua 0.4.0 até à validação final, como nas fases anteriores.

# Ficha 03 — Exercício 8: Verificar o MVP

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Demonstrar que o MVP cumpre os requisitos e conserva os dados (incremento I-7).

# Context
Ficha 03, Ex. 8 (9 cenários), docs/implementation-plan.md (I-7), testes existentes (backend e frontend),
aplicação completa depois do Ex. 7.

# Constraints
- Registar em docs/verification.md o esperado, o observado e a evidência de cada cenário.
- Distinguir testes automáticos de verificações manuais; dizer o que não foi possível executar.
- Pelo menos 3 testes automáticos: validação, autorização e criação com consulta posterior.
- Quando aparecer um bug: reproduzir, recolher evidência, investigar a causa e só depois aplicar uma
  correção limitada, com teste de regressão.

# Task
Correr os testes; percorrer os 9 cenários pela API (incluindo ids trocados) e no browser, reiniciando o
backend para a persistência; corrigir o que falhar; escrever a verificação.

# Validation
- ./mvnw test, npm test, npm run build, npm run lint.
- Evidência de cada cenário (pedidos e respostas, capturas).
```

## Plano apresentado

1. Correr todos os testes automáticos e confirmar que os três exigidos estão cobertos.
2. Percorrer os 9 cenários pela API (incluindo trocar ids para testar a autorização) e no browser, com reinício do backend.
3. Se aparecer um bug, diagnóstico primeiro, depois correção limitada com teste de regressão.
4. Registar tudo em `docs/verification.md`.

## Resultado

| Ficheiro | Alteração |
|----------|-----------|
| `docs/verification.md` | Novo: ambiente, 9 cenários (esperado / observado / evidência / tipo), excertos dos pedidos, bug e correção, testes, limitações e observações |
| `docs/verificacao/*.png` | 6 capturas do browser (login inválido, pesquisa a 390 px, tarefa criada, detalhe com estado, erro que mantém os dados, tarefa eliminada) |
| `backend/.../services/Texto.java` | Novo: limpeza de texto que também trata espaços não separáveis |
| `ServicoTarefas`, `ServicoProjetos`, `ServicoComentarios` | Usam `Texto.limpo` em vez de `isBlank()` + `trim()` |
| `TarefasTest`, `ProjetosTest`, `ResponsaveisEComentariosTest` | Casos de regressão para o bug |
| `docs/implementation-plan.md`, `README.md` | I-7 feito; estado do projeto |

## Investigação do bug (pela ordem em que aconteceu)

1. **Reprodução:** no cenário 04, `POST /projetos/4/tarefas` com `{"titulo":"  "}` devolveu `201` e uma tarefa com título `"  "`. Com `"   "` e com `"\t\n"` a resposta era `400`.
2. **Hipóteses:** (a) a validação usa um método que não conhece estes caracteres; (b) o JSON era descodificado de forma errada. A resposta trazia os dois U+00A0 intactos, o que afasta (b).
3. **Localização:** `ServicoTarefas.tituloValido`, `ServicoProjetos.nomeValido`/`descricaoValida` e `ServicoComentarios.conteudoValido` usam `isBlank()` + `trim()`.
4. **Causa confirmada:** `String.trim()` só retira caracteres ≤ U+0020 e `String.isBlank()` usa `Character.isWhitespace`, que exclui os espaços não separáveis (U+00A0, U+2007, U+202F). No frontend o `trim()` do JavaScript já os retira, por isso só a API estava exposta.
5. **Teste de regressão primeiro:** acrescentei os casos aos três testes de validação e corri-os: 3 falhas (`Status expected:<400> but was:<201>`).
6. **Correção limitada:** `Texto.limpo` retira das pontas o mesmo que o `trim()` mais `\p{Z}` e o BOM. Os 71 testes passam e o pedido manual passou a dar `400`; `" Com NBSP "` fica guardado como `"Com NBSP"`.

## Validação

| Verificação | Resultado |
|-------------|-----------|
| `./mvnw test` | ✅ 71 testes, 0 falhas (os casos de regressão entraram nos 3 testes de validação existentes). *Corrigido no Ex. 9: dizia "74" por engano.* |
| `npm test` | ✅ 22 testes, 0 falhas |
| `npm run build`, `npm run lint` | ✅ sem erros nem avisos |
| 9 cenários | ✅ todos, o 04 depois da correção; ver `docs/verification.md` |
| Persistência | ✅ tarefa, estado, prioridade, responsável e comentário iguais depois de reiniciar o backend |
| 390 px | ✅ painel e projeto sem scroll horizontal; sem erros na consola |

## Decisões e observações (a validar)

- **Correção do bug feita antes da tua revisão:** o diagnóstico está acima e no `docs/verification.md` §5; a correção é pequena e pode ser revertida se preferires outra abordagem.
- **Capturas no repositório** (`docs/verificacao/`, cerca de 480 KB), como evidência do relatório.
- **Não corrigido, fica para decidir:** eliminar um projeto não mostra mensagem de sucesso no painel (eliminar uma tarefa mostra); os filtros da lista de tarefas perdem-se ao voltar do detalhe. Podem entrar na revisão do Ex. 9.

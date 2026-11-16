package pt.matriz.dtos;

/**
 * Schema {@code Conta} do contrato: o utilizador autenticado, com o plano. Só sai em {@code /auth/*} (o plano de
 * uma pessoa não aparece aos outros membros). {@code podeCriarProjetos} diz ao frontend se mostra o botão
 * "Novo projeto"; a regra é sempre verificada no backend.
 */
public record ContaResposta(String id, String nome, String email, String plano, boolean podeCriarProjetos) {
}

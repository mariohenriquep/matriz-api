package pt.matriz.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import pt.matriz.TesteIntegracao;
import pt.matriz.domain.Papel;
import pt.matriz.exceptions.ErroApi;

/**
 * Ficha 03, Ex. 3: regras de acesso com utilizadores diferentes, sobre os dados do seed.
 * Ana: Gestora de "Website institucional", membro de "Formação interna", sem acesso a "App móvel".
 */
class ControloAcessoTest extends TesteIntegracao {

    @Autowired
    ControloAcesso acesso;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void papelDependeDoProjeto() {
        long ana = utilizador("ana@matriz.local");
        assertThat(acesso.exigirMembro(projeto("Website institucional"), ana)).isEqualTo(Papel.GESTOR);
        assertThat(acesso.exigirMembro(projeto("Formação interna"), ana)).isEqualTo(Papel.MEMBRO);
    }

    @Test
    void naoMembroRecebeOMesmo404QueUmProjetoInexistente() {
        long ana = utilizador("ana@matriz.local");
        ErroApi alheio = erro(() -> acesso.exigirMembro(projeto("App móvel"), ana));
        ErroApi inexistente = erro(() -> acesso.exigirMembro(999_999, ana));

        assertThat(alheio.estado()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(alheio.getMessage()).isEqualTo(inexistente.getMessage());
        assertThat(alheio.estado()).isEqualTo(inexistente.estado());
    }

    @Test
    void membroQueNaoEGestorRecebe403() {
        long ana = utilizador("ana@matriz.local");
        ErroApi erro = erro(() -> acesso.exigirGestor(projeto("Formação interna"), ana));
        assertThat(erro.estado()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void gestorPassaEOutroUtilizadorSemAcessoRecebe404() {
        acesso.exigirGestor(projeto("Website institucional"), utilizador("ana@matriz.local"));
        ErroApi erro = erro(() -> acesso.exigirGestor(projeto("Website institucional"), utilizador("joana@matriz.local")));
        assertThat(erro.estado()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void tarefasSeguemAsRegrasDoProjeto() {
        long tarefaDaApp = jdbc.queryForObject("SELECT id FROM tarefa WHERE titulo = 'Levantar os requisitos com o cliente'",
                Long.class);
        assertThat(acesso.exigirMembroDaTarefa(tarefaDaApp, utilizador("joana@matriz.local")))
                .isEqualTo(projeto("App móvel"));

        ErroApi alheia = erro(() -> acesso.exigirMembroDaTarefa(tarefaDaApp, utilizador("ana@matriz.local")));
        ErroApi inexistente = erro(() -> acesso.exigirMembroDaTarefa(999_999, utilizador("ana@matriz.local")));
        assertThat(alheia.estado()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(alheia.getMessage()).isEqualTo(inexistente.getMessage());
    }

    private long utilizador(String email) {
        return jdbc.queryForObject("SELECT id FROM utilizador WHERE email = ?", Long.class, email);
    }

    private long projeto(String nome) {
        return jdbc.queryForObject("SELECT id FROM projeto WHERE nome = ?", Long.class, nome);
    }

    private static ErroApi erro(Runnable operacao) {
        Throwable erro = catchThrowable(operacao::run);
        assertThat(erro).isInstanceOf(ErroApi.class);
        return (ErroApi) erro;
    }
}

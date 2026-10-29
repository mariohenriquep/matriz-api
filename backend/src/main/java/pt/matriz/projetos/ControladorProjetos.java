package pt.matriz.projetos;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import pt.matriz.identidade.UtilizadorAtual;

/** {@code /projetos} e {@code /projetos/{id}/membros} do openapi.yaml. */
@RestController
@RequestMapping("/api/v1/projetos")
public class ControladorProjetos {

    private final ServicoProjetos servico;
    private final UtilizadorAtual utilizadorAtual;

    public ControladorProjetos(ServicoProjetos servico, UtilizadorAtual utilizadorAtual) {
        this.servico = servico;
        this.utilizadorAtual = utilizadorAtual;
    }

    @GetMapping
    public Lista<ProjetoResposta> listar(@RequestParam(required = false) String pesquisa) {
        return Lista.de(servico.listar(utilizadorAtual.id(), pesquisa).stream().map(ProjetoResposta::de).toList());
    }

    @PostMapping
    public ResponseEntity<ProjetoResposta> criar(@RequestBody NovoProjeto pedido) {
        ProjetoVista projeto = servico.criar(utilizadorAtual.id(), pedido.nome(), pedido.descricao());
        return ResponseEntity.created(URI.create("/api/v1/projetos/" + projeto.id())).body(ProjetoResposta.de(projeto));
    }

    @GetMapping("/{projetoId}")
    public ProjetoResposta obter(@PathVariable long projetoId) {
        return ProjetoResposta.de(servico.obter(projetoId, utilizadorAtual.id()));
    }

    @PatchMapping("/{projetoId}")
    public ProjetoResposta editar(@PathVariable long projetoId, @RequestBody AlteracaoProjeto alteracao) {
        return ProjetoResposta.de(servico.editar(projetoId, utilizadorAtual.id(), alteracao));
    }

    @DeleteMapping("/{projetoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long projetoId) {
        servico.eliminar(projetoId, utilizadorAtual.id());
    }

    @GetMapping("/{projetoId}/membros")
    public Lista<MembroResposta> membros(@PathVariable long projetoId) {
        return Lista.de(servico.membros(projetoId, utilizadorAtual.id()).stream().map(MembroResposta::de).toList());
    }

    @PostMapping("/{projetoId}/membros")
    @ResponseStatus(HttpStatus.CREATED)
    public MembroResposta adicionarMembro(@PathVariable long projetoId, @Valid @RequestBody NovoMembro pedido) {
        return MembroResposta.de(servico.adicionarMembro(projetoId, utilizadorAtual.id(), pedido.email()));
    }

    @DeleteMapping("/{projetoId}/membros/{utilizadorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerMembro(@PathVariable long projetoId, @PathVariable long utilizadorId) {
        servico.removerMembro(projetoId, utilizadorAtual.id(), utilizadorId);
    }

    public record NovoProjeto(String nome, String descricao) {
    }

    public record NovoMembro(
            @NotBlank(message = "O email é obrigatório") @Email(message = "O email não é válido") String email) {
    }

    public record Lista<T>(List<T> itens, int total) {

        static <T> Lista<T> de(List<T> itens) {
            return new Lista<>(itens, itens.size());
        }
    }

    public record ResumoUtilizador(String id, String nome) {
    }

    public record DadosUtilizador(String id, String nome, String email) {
    }

    /** Nomes das chaves como no schema {@code ContagensPorEstado}. */
    public record Contagens(
            @JsonProperty("POR_FAZER") long porFazer,
            @JsonProperty("EM_CURSO") long emCurso,
            @JsonProperty("CONCLUIDA") long concluidas,
            long total) {
    }

    public record ProjetoResposta(String id, String nome, String descricao, ResumoUtilizador gestor, Papel meuPapel,
            Contagens contagens, OffsetDateTime criadoEm, OffsetDateTime atualizadoEm) {

        static ProjetoResposta de(ProjetoVista p) {
            return new ProjetoResposta(String.valueOf(p.id()), p.nome(), p.descricao(),
                    new ResumoUtilizador(String.valueOf(p.gestorId()), p.gestorNome()), p.meuPapel(),
                    new Contagens(p.porFazer(), p.emCurso(), p.concluidas(), p.porFazer() + p.emCurso() + p.concluidas()),
                    p.criadoEm(), p.atualizadoEm());
        }
    }

    public record MembroResposta(DadosUtilizador utilizador, Papel papel) {

        static MembroResposta de(MembroVista m) {
            return new MembroResposta(new DadosUtilizador(String.valueOf(m.id()), m.nome(), m.email()), m.papel());
        }
    }
}

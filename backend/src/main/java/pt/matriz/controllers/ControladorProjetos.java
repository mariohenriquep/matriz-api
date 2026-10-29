package pt.matriz.controllers;

import java.net.URI;

import jakarta.validation.Valid;

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

import pt.matriz.domain.Projeto;
import pt.matriz.dtos.AlteracaoProjeto;
import pt.matriz.dtos.Lista;
import pt.matriz.dtos.MembroResposta;
import pt.matriz.dtos.NovoMembro;
import pt.matriz.dtos.NovoProjeto;
import pt.matriz.dtos.ProjetoResposta;
import pt.matriz.mappers.ProjetoMapper;
import pt.matriz.security.UtilizadorAtual;
import pt.matriz.services.ServicoProjetos;

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
        return Lista.de(servico.listar(utilizadorAtual.id(), pesquisa).stream()
                .map(ProjetoMapper::paraResposta).toList());
    }

    @PostMapping
    public ResponseEntity<ProjetoResposta> criar(@RequestBody NovoProjeto pedido) {
        Projeto projeto = servico.criar(utilizadorAtual.id(), pedido.nome(), pedido.descricao());
        return ResponseEntity.created(URI.create("/api/v1/projetos/" + projeto.id()))
                .body(ProjetoMapper.paraResposta(projeto));
    }

    @GetMapping("/{projetoId}")
    public ProjetoResposta obter(@PathVariable long projetoId) {
        return ProjetoMapper.paraResposta(servico.obter(projetoId, utilizadorAtual.id()));
    }

    @PatchMapping("/{projetoId}")
    public ProjetoResposta editar(@PathVariable long projetoId, @RequestBody AlteracaoProjeto alteracao) {
        return ProjetoMapper.paraResposta(
                servico.editar(projetoId, utilizadorAtual.id(), ProjetoMapper.paraEdicao(alteracao)));
    }

    @DeleteMapping("/{projetoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long projetoId) {
        servico.eliminar(projetoId, utilizadorAtual.id());
    }

    @GetMapping("/{projetoId}/membros")
    public Lista<MembroResposta> membros(@PathVariable long projetoId) {
        return Lista.de(servico.membros(projetoId, utilizadorAtual.id()).stream()
                .map(ProjetoMapper::paraResposta).toList());
    }

    @PostMapping("/{projetoId}/membros")
    @ResponseStatus(HttpStatus.CREATED)
    public MembroResposta adicionarMembro(@PathVariable long projetoId, @Valid @RequestBody NovoMembro pedido) {
        return ProjetoMapper.paraResposta(servico.adicionarMembro(projetoId, utilizadorAtual.id(), pedido.email()));
    }

    @DeleteMapping("/{projetoId}/membros/{utilizadorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerMembro(@PathVariable long projetoId, @PathVariable long utilizadorId) {
        servico.removerMembro(projetoId, utilizadorAtual.id(), utilizadorId);
    }
}

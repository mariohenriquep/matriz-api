package pt.matriz.mappers;

import pt.matriz.domain.ContagensTarefas;
import pt.matriz.domain.EdicaoProjeto;
import pt.matriz.domain.Membro;
import pt.matriz.domain.Projeto;
import pt.matriz.dtos.AlteracaoProjeto;
import pt.matriz.dtos.Contagens;
import pt.matriz.dtos.MembroResposta;
import pt.matriz.dtos.ProjetoResposta;
import pt.matriz.dtos.ResumoUtilizador;
import pt.matriz.dtos.UtilizadorResposta;

/** Converte projetos e membros entre o domínio e os DTOs de {@code /projetos}. */
public final class ProjetoMapper {

    private ProjetoMapper() {
    }

    public static ProjetoResposta paraResposta(Projeto projeto) {
        return new ProjetoResposta(String.valueOf(projeto.id()), projeto.nome(), projeto.descricao(),
                new ResumoUtilizador(String.valueOf(projeto.gestorId()), projeto.gestorNome()), projeto.meuPapel(),
                paraResposta(projeto.contagens()), projeto.criadoEm(), projeto.atualizadoEm());
    }

    public static Contagens paraResposta(ContagensTarefas contagens) {
        return new Contagens(contagens.porFazer(), contagens.emCurso(), contagens.concluidas(), contagens.total());
    }

    public static MembroResposta paraResposta(Membro membro) {
        return new MembroResposta(new UtilizadorResposta(String.valueOf(membro.id()), membro.nome(), membro.email()),
                membro.papel());
    }

    public static EdicaoProjeto paraEdicao(AlteracaoProjeto alteracao) {
        return new EdicaoProjeto(alteracao.temNome(), alteracao.nome(), alteracao.temDescricao(),
                alteracao.descricao());
    }
}

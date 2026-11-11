package pt.matriz.repositories;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import pt.matriz.domain.Utilizador;

/** Utilizadores, através de Spring Data JPA (a única entidade JPA do projeto). */
public interface UtilizadorRepositorio extends JpaRepository<Utilizador, Long> {

    /** O email é único sem distinguir maiúsculas (CE-08). */
    Optional<Utilizador> findByEmailIgnoreCase(String email);

    /**
     * Regista uma tentativa falhada numa só instrução (seguro com pedidos em paralelo). À {@code maximo}-ésima
     * seguida, o login fica bloqueado até {@code bloquearAte} e o contador volta a zero (RNF-11).
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE utilizador
               SET tentativas_falhadas = CASE WHEN tentativas_falhadas + 1 >= :maximo THEN 0
                                              ELSE tentativas_falhadas + 1 END,
                   bloqueado_ate = CASE WHEN tentativas_falhadas + 1 >= :maximo THEN :bloquearAte
                                        ELSE bloqueado_ate END
             WHERE id = :id""", nativeQuery = true)
    void registarTentativaFalhada(@Param("id") long id, @Param("maximo") int maximo,
            @Param("bloquearAte") OffsetDateTime bloquearAte);

    /** Login com sucesso: as tentativas falhadas deixam de contar como "seguidas". */
    @Modifying
    @Transactional
    @Query(value = "UPDATE utilizador SET tentativas_falhadas = 0, bloqueado_ate = NULL WHERE id = :id"
            + " AND (tentativas_falhadas > 0 OR bloqueado_ate IS NOT NULL)", nativeQuery = true)
    void limparTentativas(@Param("id") long id);
}

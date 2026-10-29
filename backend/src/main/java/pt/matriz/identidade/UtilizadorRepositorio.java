package pt.matriz.identidade;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilizadorRepositorio extends JpaRepository<Utilizador, Long> {

    /** O email é único sem distinguir maiúsculas (CE-08). */
    Optional<Utilizador> findByEmailIgnoreCase(String email);
}

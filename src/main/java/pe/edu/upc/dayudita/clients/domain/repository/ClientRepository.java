package pe.edu.upc.dayudita.clients.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.clients.domain.model.Client;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByEmail(String email);

    Optional<Client> findByEmailIgnoreCase(String email);

    Optional<Client> findByDocumentNumber(String documentNumber);

    boolean existsByEmail(String email);

    boolean existsByDocumentNumber(String documentNumber);
}

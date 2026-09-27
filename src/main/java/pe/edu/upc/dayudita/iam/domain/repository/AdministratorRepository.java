//Interface to access the database that extends JPARepository
package pe.edu.upc.dayudita.iam.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.iam.domain.model.Administrator;

import java.util.Optional;

public interface AdministratorRepository extends JpaRepository<Administrator,Long> {
    Optional<Administrator> findByEmail(String email);

    boolean existsByEmail(String email);
}

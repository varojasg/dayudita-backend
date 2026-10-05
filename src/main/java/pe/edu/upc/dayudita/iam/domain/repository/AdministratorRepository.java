package pe.edu.upc.dayudita.iam.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.iam.domain.model.Administrator;
import pe.edu.upc.dayudita.iam.domain.model.AdministratorRole;

import java.util.Optional;

public interface AdministratorRepository extends JpaRepository<Administrator,Long> {
    Optional<Administrator> findByEmail(String email);

    Optional<Administrator> findByEmailIgnoreCase(String email);

    Optional<Administrator> findFirstByRole(AdministratorRole role);

    Optional<Administrator> findByStore_Id(Long storeId);

    boolean existsByEmail(String email);

    boolean existsByRole(AdministratorRole role);
}

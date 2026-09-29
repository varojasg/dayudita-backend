package pe.edu.upc.dayudita.clients.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;

import java.util.List;
import java.util.Optional;

public interface ClientAccountRepository extends JpaRepository<ClientAccount, Long> {

    List<ClientAccount> findByStore_Id(Long storeId);

    List<ClientAccount> findByStore_IdAndActiveTrue(Long storeId);

    List<ClientAccount> findByClient_IdAndActiveTrue(Long clientId);

    Optional<ClientAccount> findByClient_IdAndStore_Id(Long clientId, Long storeId);
}

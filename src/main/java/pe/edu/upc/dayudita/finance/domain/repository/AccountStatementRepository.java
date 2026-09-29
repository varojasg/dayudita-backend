package pe.edu.upc.dayudita.finance.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.finance.domain.model.AccountStatement;

import java.time.LocalDate;
import java.util.List;

public interface AccountStatementRepository extends JpaRepository<AccountStatement, Long> {

    boolean existsByClientAccount_IdAndCutoffDate(Long clientAccountId, LocalDate cutoffDate);

    List<AccountStatement> findByClientAccount_Client_IdOrderByCutoffDateDesc(Long clientId);

    List<AccountStatement> findByClientAccount_Store_IdAndClientAccount_Client_IdOrderByCutoffDateDesc(
            Long storeId,
            Long clientId
    );
}

package pe.edu.upc.dayudita.finance.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.finance.domain.model.CreditPlan;
import pe.edu.upc.dayudita.finance.domain.model.CreditPlanStatus;

import java.util.List;
import java.util.Optional;

public interface CreditPlanRepository extends JpaRepository<CreditPlan, Long> {

    Optional<CreditPlan> findByPurchase_Id(Long purchaseId);

    List<CreditPlan> findByPurchase_ClientAccount_Client_IdOrderByCreatedAtDesc(Long clientId);

    Optional<CreditPlan> findFirstByPurchase_ClientAccount_Client_IdAndStatus(
            Long clientId,
            CreditPlanStatus status
    );
}

package pe.edu.upc.dayudita.finance.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.finance.domain.model.Installment;
import pe.edu.upc.dayudita.finance.domain.model.InstallmentStatus;

import java.util.List;
import java.util.Optional;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {

    List<Installment> findByCreditPlan_IdOrderByInstallmentNumberAsc(Long creditPlanId);

    Optional<Installment> findByCreditPlan_IdAndInstallmentNumber(Long creditPlanId, Integer installmentNumber);

    boolean existsByCreditPlan_IdAndStatus(Long creditPlanId, InstallmentStatus status);
}

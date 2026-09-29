package pe.edu.upc.dayudita.finance.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.finance.domain.model.FinancialConfiguration;

import java.util.Optional;

public interface FinancialConfigurationRepository extends JpaRepository<FinancialConfiguration, Long> {
    Optional<FinancialConfiguration> findFirstByOrderByIdAsc();
}

package pe.edu.upc.dayudita.finance.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.finance.domain.model.StatementItem;

import java.util.List;

public interface StatementItemRepository extends JpaRepository<StatementItem, Long> {
    List<StatementItem> findByStatement_IdOrderByPurchase_PurchaseDateAsc(Long statementId);
}

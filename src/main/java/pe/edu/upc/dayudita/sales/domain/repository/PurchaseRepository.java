package pe.edu.upc.dayudita.sales.domain.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.sales.domain.model.Purchase;
import pe.edu.upc.dayudita.sales.domain.model.PurchasePaymentMode;
import pe.edu.upc.dayudita.sales.domain.model.PurchaseStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @EntityGraph(attributePaths = "details")
    List<Purchase> findByClientAccount_Store_IdOrderByPurchaseDateDesc(Long storeId);

    @EntityGraph(attributePaths = "details")
    List<Purchase> findByClientAccount_Client_IdOrderByPurchaseDateDesc(Long clientId);

    List<Purchase> findByClientAccount_Client_IdAndPaymentModeAndStatusIn(
            Long clientId,
            PurchasePaymentMode paymentMode,
            Collection<PurchaseStatus> statuses
    );

    List<Purchase> findByClientAccount_IdAndPaymentModeAndStatusAndPurchaseDateLessThanEqualOrderByPurchaseDateAsc(
            Long clientAccountId,
            PurchasePaymentMode paymentMode,
            PurchaseStatus status,
            LocalDate purchaseDate
    );
}

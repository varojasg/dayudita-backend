package pe.edu.upc.dayudita.finance.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.dayudita.finance.domain.model.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInstallment_CreditPlan_Purchase_ClientAccount_Client_IdOrderByPaymentDateDesc(Long clientId);
}

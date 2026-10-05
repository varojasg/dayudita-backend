package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.finance.domain.model.*;
import pe.edu.upc.dayudita.finance.domain.repository.*;
import pe.edu.upc.dayudita.iam.application.CurrentUserService;
import pe.edu.upc.dayudita.sales.domain.model.PurchaseStatus;
import pe.edu.upc.dayudita.sales.domain.repository.PurchaseRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private final CreditPlanRepository creditPlanRepository;
    private final InstallmentRepository installmentRepository;
    private final PaymentRepository paymentRepository;
    private final PurchaseRepository purchaseRepository;
    private final CreditPlanService creditPlanService;
    private final CurrentUserService currentUserService;

    public PaymentService(
            CreditPlanRepository creditPlanRepository,
            InstallmentRepository installmentRepository,
            PaymentRepository paymentRepository,
            PurchaseRepository purchaseRepository,
            CreditPlanService creditPlanService,
            CurrentUserService currentUserService
    ){
        this.creditPlanRepository = creditPlanRepository;
        this.installmentRepository = installmentRepository;
        this.paymentRepository = paymentRepository;
        this.purchaseRepository = purchaseRepository;
        this.creditPlanService = creditPlanService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Payment payInstallment(Long storeId, Long installmentId, LocalDate paymentDate){
        currentUserService.validateStoreAdmin(storeId);

        Installment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new IllegalArgumentException("La cuota no existe"));

        CreditPlan plan = installment.getCreditPlan();

        if(!plan.getPurchase().getClientAccount().getStore().getId().equals(storeId)){
            throw new IllegalArgumentException("La cuota no pertenece a esta tienda");
        }

        if(installment.getType() == GraceType.TOTAL){
            throw new IllegalArgumentException("Esta cuota es de gracia total y no requiere pago");
        }

        if(installment.getStatus() == InstallmentStatus.PAID){
            throw new IllegalArgumentException("La cuota ya fue pagada");
        }

        if(installment.getInstallmentNumber() > 1){
            Installment previousInstallment = installmentRepository
                    .findByCreditPlan_IdAndInstallmentNumber(
                            plan.getId(),
                            installment.getInstallmentNumber() - 1
                    )
                    .orElseThrow(() -> new IllegalArgumentException("No se encontro la cuota anterior"));

            if(previousInstallment.getStatus() != InstallmentStatus.PAID){
                throw new IllegalArgumentException("Primero se debe pagar la cuota anterior");
            }
        }

        CreditPlanService.MoratoryBreakdown breakdown = creditPlanService.computeMoratoryBreakdown(
                installment,
                plan.getTeaMoratoriaPactada(),
                paymentDate
        );

        Payment payment = new Payment();
        payment.setInstallment(installment);
        payment.setPaymentDate(paymentDate);
        payment.setTeaMoratoriaPactada(plan.getTeaMoratoriaPactada());
        payment.setDiasMora(breakdown.diasMora());
        payment.setAmortizacion(installment.getAmortization());
        payment.setInteres(installment.getInterest());
        payment.setInteresMoratorio(breakdown.interesMoratorio());
        payment.setTotalAmount(breakdown.pagoTotal());
        payment = paymentRepository.save(payment);

        installment.setStatus(InstallmentStatus.PAID);
        installment.setPaymentDate(paymentDate);
        installmentRepository.save(installment);

        if(!installmentRepository.existsByCreditPlan_IdAndStatus(plan.getId(), InstallmentStatus.PENDING)){
            plan.setStatus(CreditPlanStatus.PAID);
            creditPlanRepository.save(plan);

            plan.getPurchase().setStatus(PurchaseStatus.PAID);
            purchaseRepository.save(plan.getPurchase());
        }

        return payment;
    }

    public List<Payment> getPaymentsByClient(Long clientId){
        return paymentRepository
                .findByInstallment_CreditPlan_Purchase_ClientAccount_Client_IdOrderByPaymentDateDesc(clientId);
    }
}

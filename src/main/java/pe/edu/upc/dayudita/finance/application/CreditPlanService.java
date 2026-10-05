package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;
import pe.edu.upc.dayudita.finance.domain.model.*;
import pe.edu.upc.dayudita.finance.domain.repository.CreditPlanRepository;
import pe.edu.upc.dayudita.finance.domain.repository.InstallmentRepository;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.CreditPlanResponse;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.InstallmentDisplayStatus;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.InstallmentResponse;
import pe.edu.upc.dayudita.sales.domain.model.Purchase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreditPlanService {

    public static final int PAYMENT_PERIOD_DAYS = FinancialCalculator.PAYMENT_PERIOD_DAYS;
    private static final int MONTHS_PER_PERIOD = 15;
    private static final int DAYS_PER_MONTH = 30;

    private final CreditPlanRepository creditPlanRepository;
    private final InstallmentRepository installmentRepository;
    private final FinancialCalculator financialCalculator;
    private final FinancialDateService financialDateService;

    public CreditPlanService(
            CreditPlanRepository creditPlanRepository,
            InstallmentRepository installmentRepository,
            FinancialCalculator financialCalculator,
            FinancialDateService financialDateService
    ){
        this.creditPlanRepository = creditPlanRepository;
        this.installmentRepository = installmentRepository;
        this.financialCalculator = financialCalculator;
        this.financialDateService = financialDateService;
    }

    public int totalInstallments(int numeroMeses){
        return numeroMeses * DAYS_PER_MONTH / MONTHS_PER_PERIOD;
    }

    @Transactional
    public CreditPlan createPlan(
            Purchase purchase,
            BigDecimal ventaPrecio,
            BigDecimal porcentajeCuotaInicial,
            int numeroMeses,
            boolean aplicaGracia
    ){
        ClientAccount account = purchase.getClientAccount();
        BigDecimal teaPactada = account.getTeaPactada();
        int n = totalInstallments(numeroMeses);

        BigDecimal cuotaInicial = financialCalculator.money(ventaPrecio.multiply(porcentajeCuotaInicial));
        BigDecimal principal = financialCalculator.money(ventaPrecio.subtract(cuotaInicial));

        Schedule schedule = buildSchedule(
                principal,
                teaPactada,
                n,
                aplicaGracia,
                purchase.getPurchaseDate(),
                account.getDiaCorte()
        );

        CreditPlan plan = new CreditPlan();
        plan.setPurchase(purchase);
        plan.setVentaPrecio(financialCalculator.money(ventaPrecio));
        plan.setPorcentajeCuotaInicial(porcentajeCuotaInicial);
        plan.setCuotaInicial(cuotaInicial);
        plan.setPrincipal(principal);
        plan.setTeaPactada(teaPactada);
        plan.setTeq(schedule.teq());
        plan.setTeaMoratoriaPactada(account.getTeaMoratoriaPactada());
        plan.setNumeroMeses(numeroMeses);
        plan.setInstallmentCount(n + (schedule.diasGraciaTotal() > 0 ? 1 : 0));
        plan.setDiasGraciaTotal(schedule.diasGraciaTotal());
        plan.setTotalIntereses(schedule.totalIntereses());
        plan.setInteresCapitalizado(schedule.interesCapitalizado());
        plan.setTotalAmortizacion(schedule.totalAmortizacion());
        plan.setTotalCuotas(schedule.totalCuotas());
        plan.setTotalAPagar(financialCalculator.money(cuotaInicial.add(schedule.totalCuotas())));

        plan = creditPlanRepository.save(plan);

        for(Row row : schedule.rows()){
            Installment installment = new Installment();
            installment.setCreditPlan(plan);
            installment.setInstallmentNumber(row.k());
            installment.setType(row.type());
            installment.setDueDate(row.dueDate());
            installment.setOpeningBalance(row.openingBalance());
            installment.setInterest(row.interest());
            installment.setAmount(row.payment());
            installment.setAmortization(row.amortization());
            installment.setRemainingBalance(row.remainingBalance());
            // Las cuotas de gracia total no requieren accion de pago (R=0):
            // se marcan resueltas desde el inicio para que el orden de pago
            // y el cierre del plan no necesiten un caso especial para ellas.
            installment.setStatus(row.type() == GraceType.TOTAL ? InstallmentStatus.PAID : InstallmentStatus.PENDING);
            installmentRepository.save(installment);
        }

        return plan;
    }

    public CreditPlanResponse simulate(
            BigDecimal ventaPrecio,
            BigDecimal porcentajeCuotaInicial,
            int numeroMeses,
            boolean aplicaGracia,
            BigDecimal teaPactada,
            BigDecimal teaMoratoriaPactada,
            LocalDate purchaseDate,
            int diaCorte
    ){
        int n = totalInstallments(numeroMeses);
        BigDecimal cuotaInicial = financialCalculator.money(ventaPrecio.multiply(porcentajeCuotaInicial));
        BigDecimal principal = financialCalculator.money(ventaPrecio.subtract(cuotaInicial));

        Schedule schedule = buildSchedule(
                principal,
                teaPactada,
                n,
                aplicaGracia,
                purchaseDate,
                diaCorte
        );

        List<InstallmentResponse> installments = new ArrayList<>();
        for(Row row : schedule.rows()){
            installments.add(new InstallmentResponse(
                    null,
                    row.k(),
                    row.type(),
                    row.dueDate(),
                    row.rate(),
                    row.days(),
                    row.openingBalance(),
                    row.interest(),
                    row.payment(),
                    row.amortization(),
                    row.remainingBalance(),
                    0,
                    BigDecimal.ZERO.setScale(9),
                    financialCalculator.money(BigDecimal.ZERO),
                    financialCalculator.money(row.payment()),
                    row.type() == GraceType.TOTAL ? InstallmentDisplayStatus.GRACE_TOTAL : InstallmentDisplayStatus.PENDING,
                    null
            ));
        }

        BigDecimal totalAPagar = financialCalculator.money(cuotaInicial.add(schedule.totalCuotas()));

        return new CreditPlanResponse(
                null,
                null,
                null,
                null,
                financialCalculator.money(ventaPrecio),
                porcentajeCuotaInicial,
                cuotaInicial,
                principal,
                teaPactada,
                schedule.teq(),
                teaMoratoriaPactada,
                PAYMENT_PERIOD_DAYS,
                numeroMeses,
                n + (schedule.diasGraciaTotal() > 0 ? 1 : 0),
                schedule.diasGraciaTotal(),
                schedule.totalIntereses(),
                schedule.interesCapitalizado(),
                schedule.totalAmortizacion(),
                schedule.totalCuotas(),
                totalAPagar,
                financialCalculator.money(BigDecimal.ZERO),
                totalAPagar,
                null,
                installments
        );
    }

    public CreditPlanResponse toCreditPlanResponse(CreditPlan plan){
        ClientAccount account = plan.getPurchase().getClientAccount();
        LocalDate today = LocalDate.now();

        List<InstallmentResponse> installmentResponses = new ArrayList<>();
        BigDecimal totalInteresMoratorio = BigDecimal.ZERO;

        for(Installment installment : getInstallments(plan.getId())){
            MoratoryBreakdown breakdown = computeMoratoryBreakdown(
                    installment,
                    account.getTeaMoratoriaPactada(),
                    today
            );

            BigDecimal tasaPeriodo = installment.getType() == GraceType.TOTAL
                    ? financialCalculator.effectiveRateForDays(plan.getTeaPactada(), plan.getDiasGraciaTotal())
                    : plan.getTeq();
            int diasPeriodo = installment.getType() == GraceType.TOTAL
                    ? plan.getDiasGraciaTotal()
                    : PAYMENT_PERIOD_DAYS;

            installmentResponses.add(new InstallmentResponse(
                    installment.getId(),
                    installment.getInstallmentNumber(),
                    installment.getType(),
                    installment.getDueDate(),
                    tasaPeriodo,
                    diasPeriodo,
                    installment.getOpeningBalance(),
                    installment.getInterest(),
                    installment.getAmount(),
                    installment.getAmortization(),
                    installment.getRemainingBalance(),
                    breakdown.diasMora(),
                    breakdown.tasaMoratoriaTramo(),
                    breakdown.interesMoratorio(),
                    breakdown.pagoTotal(),
                    resolveDisplayStatus(installment, today),
                    installment.getPaymentDate()
            ));

            totalInteresMoratorio = totalInteresMoratorio.add(breakdown.interesMoratorio());
        }

        totalInteresMoratorio = financialCalculator.money(totalInteresMoratorio);

        return new CreditPlanResponse(
                plan.getId(),
                plan.getPurchase().getId(),
                account.getClient().getId(),
                account.getStore().getId(),
                plan.getVentaPrecio(),
                plan.getPorcentajeCuotaInicial(),
                plan.getCuotaInicial(),
                plan.getPrincipal(),
                plan.getTeaPactada(),
                plan.getTeq(),
                plan.getTeaMoratoriaPactada(),
                PAYMENT_PERIOD_DAYS,
                plan.getNumeroMeses(),
                plan.getInstallmentCount(),
                plan.getDiasGraciaTotal(),
                plan.getTotalIntereses(),
                plan.getInteresCapitalizado(),
                plan.getTotalAmortizacion(),
                plan.getTotalCuotas(),
                plan.getTotalAPagar(),
                totalInteresMoratorio,
                financialCalculator.money(plan.getTotalAPagar().add(totalInteresMoratorio)),
                plan.getStatus(),
                installmentResponses
        );
    }

    /**
     * Mora y pago exacto de una cuota evaluados a una fecha dada (la fecha
     * de pago real si se esta registrando un pago, o hoy si solo se esta
     * consultando el cronograma).
     */
    public MoratoryBreakdown computeMoratoryBreakdown(
            Installment installment,
            BigDecimal teaMoratoriaPactada,
            LocalDate asOfDate
    ){
        int diasMora = resolveOverdueDays(installment, asOfDate);
        BigDecimal tasaMoratoriaTramo = financialCalculator.moratoryTrancheRate(teaMoratoriaPactada, diasMora);
        BigDecimal interesMoratorio = financialCalculator.moratoryInterest(installment.getAmount(), tasaMoratoriaTramo);
        BigDecimal pagoTotal = financialCalculator.totalPayment(installment.getAmount(), interesMoratorio);

        return new MoratoryBreakdown(
                diasMora,
                tasaMoratoriaTramo,
                financialCalculator.money(interesMoratorio),
                financialCalculator.money(pagoTotal)
        );
    }

    private int resolveOverdueDays(Installment installment, LocalDate asOfDate){
        if(installment.getType() == GraceType.TOTAL){
            return 0;
        }

        LocalDate referenceDate = installment.getPaymentDate() != null ? installment.getPaymentDate() : asOfDate;

        if(!referenceDate.isAfter(installment.getDueDate())){
            return 0;
        }

        return financialDateService.commercialDaysBetween(installment.getDueDate(), referenceDate);
    }

    private InstallmentDisplayStatus resolveDisplayStatus(Installment installment, LocalDate today){
        if(installment.getType() == GraceType.TOTAL){
            return InstallmentDisplayStatus.GRACE_TOTAL;
        }

        if(installment.getStatus() == InstallmentStatus.PAID){
            return InstallmentDisplayStatus.PAID;
        }

        if(installment.getDueDate().isBefore(today)){
            return InstallmentDisplayStatus.OVERDUE;
        }

        return InstallmentDisplayStatus.PENDING;
    }

    /**
     * Arma el cronograma a partir del dia de corte pactado con el cliente.
     * Si {@code aplicaGracia} es verdadero y la fecha de compra no cae
     * exactamente en el dia de corte, se agrega una cuota de gracia total
     * (fila 1) cuyos dias van de la fecha de compra al primer corte (dias
     * variables, segun que tan cerca este la compra del corte). El interes
     * de esa fila se capitaliza (SF = SI + I) y no requiere pago. Las n
     * cuotas estandar del metodo frances arrancan en ese corte y avanzan
     * cada 15 dias comerciales exactos (sin reenganchar a un dia fijo del
     * calendario).
     */
    private Schedule buildSchedule(
            BigDecimal principal,
            BigDecimal teaPactada,
            int n,
            boolean aplicaGracia,
            LocalDate purchaseDate,
            int diaCorte
    ){
        BigDecimal teq = financialCalculator.periodicRate(teaPactada);

        List<Row> rows = new ArrayList<>();
        BigDecimal totalIntereses = BigDecimal.ZERO;
        BigDecimal interesCapitalizado = BigDecimal.ZERO;
        BigDecimal totalAmortizacion = BigDecimal.ZERO;
        BigDecimal totalCuotas = BigDecimal.ZERO;

        BigDecimal balance = principal;
        LocalDate firstCutoff = financialDateService.firstCutoffOnOrAfter(purchaseDate, diaCorte);
        int diasGraciaTotal = aplicaGracia
                ? financialDateService.commercialDaysBetween(purchaseDate, firstCutoff)
                : 0;
        int rowNumber = 0;

        if(diasGraciaTotal > 0){
            BigDecimal graceRate = financialCalculator.effectiveRateForDays(teaPactada, diasGraciaTotal);
            BigDecimal interest = financialCalculator.installmentInterest(balance, graceRate);
            BigDecimal remainingBalance = balance.add(interest);

            rowNumber++;
            rows.add(new Row(
                    rowNumber,
                    GraceType.TOTAL,
                    firstCutoff,
                    graceRate,
                    diasGraciaTotal,
                    financialCalculator.money(balance),
                    financialCalculator.money(interest),
                    financialCalculator.money(BigDecimal.ZERO),
                    financialCalculator.money(BigDecimal.ZERO),
                    financialCalculator.money(remainingBalance)
            ));

            totalIntereses = totalIntereses.add(interest);
            interesCapitalizado = interesCapitalizado.add(interest);
            balance = remainingBalance;
        }

        /*
         * Si la tienda otorga gracia (aunque haya salido en 0 dias porque la
         * compra cayo justo en el corte), la primera cuota estandar arranca
         * recien en el SIGUIENTE corte (+15 dias comerciales desde
         * firstCutoff). Si no otorga gracia, la primera cuota estandar se
         * cobra ya en el primer corte.
         */
        LocalDate dueDate = aplicaGracia ? financialDateService.nextDueDate(firstCutoff) : firstCutoff;

        for(int k = 1; k <= n; k++){
            BigDecimal openingBalance = balance;

            BigDecimal interest = financialCalculator.installmentInterest(openingBalance, teq);
            BigDecimal payment = financialCalculator.frenchInstallmentPayment(openingBalance, teq, n - k + 1);
            BigDecimal amortization = financialCalculator.amortization(payment, interest);
            BigDecimal remainingBalance = openingBalance.subtract(amortization);

            rowNumber++;
            rows.add(new Row(
                    rowNumber,
                    GraceType.STANDARD,
                    dueDate,
                    teq,
                    PAYMENT_PERIOD_DAYS,
                    financialCalculator.money(openingBalance),
                    financialCalculator.money(interest),
                    financialCalculator.money(payment),
                    financialCalculator.money(amortization),
                    financialCalculator.money(remainingBalance)
            ));

            totalIntereses = totalIntereses.add(interest);
            totalAmortizacion = totalAmortizacion.add(amortization);
            totalCuotas = totalCuotas.add(payment);

            balance = remainingBalance;
            dueDate = financialDateService.nextDueDate(dueDate);
        }

        return new Schedule(
                teq,
                rows,
                diasGraciaTotal,
                financialCalculator.money(totalIntereses),
                financialCalculator.money(interesCapitalizado),
                financialCalculator.money(totalAmortizacion),
                financialCalculator.money(totalCuotas)
        );
    }

    public List<CreditPlan> getPlansByClient(Long clientId){
        return creditPlanRepository.findByPurchase_ClientAccount_Client_IdOrderByCreatedAtDesc(clientId);
    }

    public CreditPlan getPlanByPurchase(Long purchaseId){
        return creditPlanRepository.findByPurchase_Id(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("La compra no tiene un plan de cuotas"));
    }

    public List<Installment> getInstallments(Long creditPlanId){
        return installmentRepository.findByCreditPlan_IdOrderByInstallmentNumberAsc(creditPlanId);
    }

    public boolean hasActivePlanForClient(Long clientId){
        return creditPlanRepository
                .findFirstByPurchase_ClientAccount_Client_IdAndStatus(clientId, CreditPlanStatus.ACTIVE)
                .isPresent();
    }

    private record Row(
            int k,
            GraceType type,
            LocalDate dueDate,
            BigDecimal rate,
            int days,
            BigDecimal openingBalance,
            BigDecimal interest,
            BigDecimal payment,
            BigDecimal amortization,
            BigDecimal remainingBalance
    ) {
    }

    private record Schedule(
            BigDecimal teq,
            List<Row> rows,
            int diasGraciaTotal,
            BigDecimal totalIntereses,
            BigDecimal interesCapitalizado,
            BigDecimal totalAmortizacion,
            BigDecimal totalCuotas
    ) {
    }

    public record MoratoryBreakdown(
            int diasMora,
            BigDecimal tasaMoratoriaTramo,
            BigDecimal interesMoratorio,
            BigDecimal pagoTotal
    ) {
    }
}

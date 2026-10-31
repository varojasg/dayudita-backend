package pe.edu.upc.dayudita.finance.application;

import org.junit.jupiter.api.Test;

import pe.edu.upc.dayudita.clients.domain.model.Client;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;
import pe.edu.upc.dayudita.finance.domain.model.CreditPlan;
import pe.edu.upc.dayudita.finance.domain.model.GraceType;
import pe.edu.upc.dayudita.finance.domain.model.Installment;
import pe.edu.upc.dayudita.finance.domain.repository.CreditPlanRepository;
import pe.edu.upc.dayudita.finance.domain.repository.InstallmentRepository;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.CreditPlanResponse;
import pe.edu.upc.dayudita.finance.interfaces.rest.dto.InstallmentResponse;
import pe.edu.upc.dayudita.sales.domain.model.Purchase;
import pe.edu.upc.dayudita.stores.domain.model.Store;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FinancialCalculatorTest {

    private static final BigDecimal CAPITAL = new BigDecimal("500.00");
    private static final BigDecimal TEQ = new BigDecimal("0.007625660");

    /** Caso 1: valores internos (7 decimales) de cada periodo. */
    private static final List<Period> CASE_1 = List.of(
            new Period(GraceType.TOTAL, "500.0000000", "3.8128300", "0.0000000", "503.8128300"),
            new Period(GraceType.STANDARD, "503.8128300", "3.8419053", "74.1853128", "433.4694225"),
            new Period(GraceType.STANDARD, "433.4694225", "3.3054904", "74.1853128", "362.5896001"),
            new Period(GraceType.STANDARD, "362.5896001", "2.7649850", "74.1853128", "291.1692723"),
            new Period(GraceType.STANDARD, "291.1692723", "2.2203579", "74.1853127", "219.2043175"),
            new Period(GraceType.STANDARD, "219.2043175", "1.6715776", "74.1853128", "146.6905823"),
            new Period(GraceType.STANDARD, "146.6905823", "1.1186125", "74.1853127", "73.6238821"),
            new Period(GraceType.STANDARD, "73.6238821", "0.5614307", "74.1853128", "0.0000000")
    );

    private static final List<LocalDate> CASE_1_DUE_DATES = List.of(
            LocalDate.of(2026, 10, 21),
            LocalDate.of(2026, 11, 6),
            LocalDate.of(2026, 11, 21),
            LocalDate.of(2026, 12, 6),
            LocalDate.of(2026, 12, 21),
            LocalDate.of(2027, 1, 6),
            LocalDate.of(2027, 1, 21),
            LocalDate.of(2027, 2, 6)
    );

    private final FinancialCalculator calculator = new FinancialCalculator();

    @Test
    void case1_withGrace_firstPeriodIsTotalAndStaysInsideN(){
        CreditPlanResponse plan = simulate(LocalDate.of(2026, 10, 6), 4, true);

        assertEquals(TEQ, plan.teq());
        assertEquals(15, plan.diasGraciaTotal());
        assertEquals(8, plan.installmentCount(), "n incluye la fila T");
        assertEquals(8, plan.installments().size());

        assertSchedule(CASE_1, CASE_1_DUE_DATES, plan);
        assertInternalAmounts(CASE_1);

        assertEquals(new BigDecimal("19.30"), plan.totalIntereses());
        assertEquals(new BigDecimal("3.81"), plan.interesCapitalizado());
        assertEquals(new BigDecimal("519.30"), plan.totalCuotas());
        assertEquals(new BigDecimal("19.2971894"), sum(CASE_1, Period::interest));
        assertEquals(new BigDecimal("519.2971894"), sum(CASE_1, Period::payment));
    }

    @Test
    void case2_withoutGrace_allPeriodsAreStandard(){
        CreditPlanResponse plan = simulate(LocalDate.of(2026, 10, 6), 4, false);

        assertEquals(0, plan.diasGraciaTotal());
        assertEquals(8, plan.installmentCount());
        assertEquals(8, plan.installments().size());

        for(int j = 0; j < 8; j++){
            InstallmentResponse row = plan.installments().get(j);
            assertEquals(GraceType.STANDARD, row.type(), "periodo " + (j + 1));
            assertEquals(new BigDecimal("64.66"), row.amount(), "periodo " + (j + 1));
            assertEquals(CASE_1_DUE_DATES.get(j), row.dueDate(), "periodo " + (j + 1));
            assertEquals(15, row.diasPeriodo(), "periodo " + (j + 1));
            assertEquals(TEQ, row.tasaPeriodo(), "periodo " + (j + 1));
        }
        assertEquals(new BigDecimal("0.00"), plan.installments().get(7).remainingBalance());

        BigDecimal balance = CAPITAL;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalPayment = BigDecimal.ZERO;

        for(int j = 1; j <= 8; j++){
            BigDecimal interest = calculator.installmentInterest(balance, TEQ);
            BigDecimal payment = calculator.frenchInstallmentPayment(balance, TEQ, 8 - j + 1);

            assertEquals(new BigDecimal("64.6637240"), payment, "cuota interna periodo " + j);

            balance = balance.subtract(calculator.amortization(payment, interest));
            totalInterest = totalInterest.add(interest);
            totalPayment = totalPayment.add(payment);
        }

        assertEquals(0, BigDecimal.ZERO.compareTo(balance), "saldo final interno");
        assertEquals(new BigDecimal("17.3097920"), totalInterest);
        assertEquals(new BigDecimal("517.3097920"), totalPayment);

        assertEquals(new BigDecimal("17.31"), plan.totalIntereses());
        assertEquals(new BigDecimal("0.00"), plan.interesCapitalizado());
        assertEquals(new BigDecimal("517.31"), plan.totalCuotas());
    }

    @Test
    void case3_datesCrossingFebruaryDoNotDrift(){
        CreditPlanResponse plan = simulate(LocalDate.of(2027, 1, 14), 4, true);

        assertSchedule(
                CASE_1,
                List.of(
                        LocalDate.of(2027, 1, 29),
                        LocalDate.of(2027, 2, 14),
                        LocalDate.of(2027, 2, 28),
                        LocalDate.of(2027, 3, 14),
                        LocalDate.of(2027, 3, 29),
                        LocalDate.of(2027, 4, 14),
                        LocalDate.of(2027, 4, 29),
                        LocalDate.of(2027, 5, 14)
                ),
                plan
        );
    }

    @Test
    void case4_purchaseOnDay31CountsAsDay30(){
        CreditPlanResponse plan = simulate(LocalDate.of(2026, 10, 31), 2, true);

        assertEquals(4, plan.installmentCount());
        assertEquals(4, plan.installments().size());

        assertEquals(
                List.of(
                        LocalDate.of(2026, 11, 15),
                        LocalDate.of(2026, 11, 30),
                        LocalDate.of(2026, 12, 15),
                        LocalDate.of(2026, 12, 30)
                ),
                plan.installments().stream().map(InstallmentResponse::dueDate).toList()
        );

        assertEquals(GraceType.TOTAL, plan.installments().get(0).type());
        assertEquals(new BigDecimal("0.00"), plan.installments().get(0).amount());

        for(InstallmentResponse row : plan.installments().subList(1, 4)){
            assertEquals(GraceType.STANDARD, row.type());
            assertEquals(new BigDecimal("170.51"), row.amount(), "periodo " + row.installmentNumber());
        }

        assertEquals(new BigDecimal("0.00"), plan.installments().get(3).remainingBalance());
        assertEquals(new BigDecimal("511.52"), plan.totalCuotas());

        BigDecimal balance = new BigDecimal("503.8128300");
        BigDecimal totalPayment = BigDecimal.ZERO;
        String[] expectedPayments = {"170.5053659", "170.5053659", "170.5053660"};

        for(int j = 2; j <= 4; j++){
            BigDecimal interest = calculator.installmentInterest(balance, TEQ);
            BigDecimal payment = calculator.frenchInstallmentPayment(balance, TEQ, 4 - j + 1);

            assertEquals(new BigDecimal(expectedPayments[j - 2]), payment, "cuota interna periodo " + j);

            balance = balance.subtract(calculator.amortization(payment, interest));
            totalPayment = totalPayment.add(payment);
        }

        assertEquals(0, BigDecimal.ZERO.compareTo(balance), "saldo final interno");
        assertEquals(new BigDecimal("511.5160978"), totalPayment);
    }

    @Test
    void case5_diaCorteDoesNotAffectSchedule(){
        for(int diaCorte : new int[]{5, 15, 28}){
            CreditPlanResponse plan = createPlan(diaCorte);

            assertSchedule(CASE_1, CASE_1_DUE_DATES, plan);
            assertEquals(15, plan.diasGraciaTotal(), "diaCorte " + diaCorte);
            assertEquals(8, plan.installmentCount(), "diaCorte " + diaCorte);
            assertEquals(new BigDecimal("519.30"), plan.totalCuotas(), "diaCorte " + diaCorte);
        }
    }

    @Test
    void caseC_moratoryInterestOnStandardInstallment(){
        BigDecimal moratoryTea = new BigDecimal("0.12");
        BigDecimal installmentThreePayment = new BigDecimal("86.22");

        assertEquals(
                new BigDecimal("0.03"),
                calculator.money(calculator.moratoryInterest(
                        installmentThreePayment,
                        calculator.moratoryTrancheRate(moratoryTea, 1)
                )),
                "mora a 1 dia"
        );

        assertEquals(
                new BigDecimal("0.08"),
                calculator.money(calculator.moratoryInterest(
                        installmentThreePayment,
                        calculator.moratoryTrancheRate(moratoryTea, 3)
                )),
                "mora a 3 dias"
        );

        assertEquals(
                new BigDecimal("0.27"),
                calculator.money(calculator.moratoryInterest(
                        installmentThreePayment,
                        calculator.moratoryTrancheRate(moratoryTea, 10)
                )),
                "mora a 10 dias"
        );
    }

    private CreditPlanResponse simulate(LocalDate purchaseDate, int numeroMeses, boolean aplicaGracia){
        CreditPlanService service = new CreditPlanService(null, null, calculator, new FinancialDateService());
        return service.simulate(
                CAPITAL,
                BigDecimal.ZERO,
                numeroMeses,
                aplicaGracia,
                new BigDecimal("0.20"),
                new BigDecimal("0.12"),
                purchaseDate
        );
    }


    private CreditPlanResponse createPlan(int diaCorte){
        ClientAccount account = new ClientAccount();
        account.setClient(new Client());
        account.setStore(new Store());
        account.setTeaPactada(new BigDecimal("0.20"));
        account.setTeaMoratoriaPactada(new BigDecimal("0.12"));
        account.setDiaCorte(diaCorte);

        Purchase purchase = new Purchase();
        purchase.setClientAccount(account);
        purchase.setPurchaseDate(LocalDate.of(2026, 10, 6));

        List<Installment> saved = new ArrayList<>();

        CreditPlanRepository planRepository = mock(CreditPlanRepository.class);
        when(planRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        InstallmentRepository installmentRepository = mock(InstallmentRepository.class);
        when(installmentRepository.save(any())).thenAnswer(invocation -> {
            saved.add(invocation.getArgument(0));
            return invocation.getArgument(0);
        });
        when(installmentRepository.findByCreditPlan_IdOrderByInstallmentNumberAsc(any())).thenReturn(saved);

        CreditPlanService service = new CreditPlanService(
                planRepository,
                installmentRepository,
                calculator,
                new FinancialDateService()
        );

        CreditPlan plan = service.createPlan(purchase, CAPITAL, BigDecimal.ZERO, 4, true);

        return service.toCreditPlanResponse(plan);
    }

 
    private void assertSchedule(List<Period> expected, List<LocalDate> dueDates, CreditPlanResponse plan){
        assertEquals(expected.size(), plan.installments().size());

        for(int index = 0; index < expected.size(); index++){
            Period period = expected.get(index);
            InstallmentResponse row = plan.installments().get(index);
            String label = "periodo " + (index + 1);

            assertEquals(index + 1, row.installmentNumber(), label);
            assertEquals(period.type(), row.type(), label);
            assertEquals(dueDates.get(index), row.dueDate(), label);
            assertEquals(15, row.diasPeriodo(), label);
            assertEquals(TEQ, row.tasaPeriodo(), label);
            assertEquals(calculator.money(period.opening()), row.openingBalance(), label);
            assertEquals(calculator.money(period.interest()), row.interest(), label);
            assertEquals(calculator.money(period.payment()), row.amount(), label);
            assertEquals(calculator.money(period.closing()), row.remainingBalance(), label);
        }
    }

   
    private void assertInternalAmounts(List<Period> expected){
        int n = expected.size();
        BigDecimal balance = CAPITAL;

        for(int j = 1; j <= n; j++){
            Period period = expected.get(j - 1);
            String label = "periodo " + j;

            assertEquals(0, period.opening().compareTo(balance), label);

            BigDecimal interest = calculator.installmentInterest(balance, TEQ);
            assertEquals(period.interest(), interest, label);

            if(period.type() == GraceType.TOTAL){
                balance = balance.add(interest);
            } else {
                BigDecimal payment = calculator.frenchInstallmentPayment(balance, TEQ, n - j + 1);
                assertEquals(period.payment(), payment, label);
                balance = balance.subtract(calculator.amortization(payment, interest));
            }

            assertEquals(0, period.closing().compareTo(balance), label);
        }
    }

    private BigDecimal sum(List<Period> periods, Function<Period, BigDecimal> field){
        return periods.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private record Period(GraceType type, BigDecimal opening, BigDecimal interest, BigDecimal payment, BigDecimal closing) {

        Period(GraceType type, String opening, String interest, String payment, String closing){
            this(type, new BigDecimal(opening), new BigDecimal(interest), new BigDecimal(payment), new BigDecimal(closing));
        }
    }
}

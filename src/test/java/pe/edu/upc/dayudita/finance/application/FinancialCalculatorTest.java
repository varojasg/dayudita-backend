package pe.edu.upc.dayudita.finance.application;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Reproduce exactamente los casos A, B y C del simulador de Excel
 * (ver Prompt_Refactor_Dayudita_Metodo_Frances-2.md, sección 2).
 */
class FinancialCalculatorTest {

    private final FinancialCalculator calculator = new FinancialCalculator();

    @Test
    void caseA_withoutGrace(){
        BigDecimal c = new BigDecimal("500.00");
        BigDecimal tea = new BigDecimal("0.20");
        int n = 8;

        BigDecimal teq = calculator.periodicRate(tea);
        assertEquals(new BigDecimal("0.007625660"), teq);

        BigDecimal balance = c;
        BigDecimal totalPaid = BigDecimal.ZERO;

        for(int k = 1; k <= n; k++){
            BigDecimal opening = balance;
            BigDecimal interest = calculator.installmentInterest(opening, teq);
            BigDecimal payment = calculator.frenchInstallmentPayment(opening, teq, n - k + 1);
            BigDecimal amortization = calculator.amortization(payment, interest);
            BigDecimal remaining = opening.subtract(amortization);

            assertEquals(new BigDecimal("64.66"), calculator.money(payment), "cuota k=" + k);

            totalPaid = totalPaid.add(payment);
            balance = remaining;
        }

        assertEquals(new BigDecimal("0.00"), calculator.money(balance), "saldo final k=8");
        assertEquals(new BigDecimal("517.31"), calculator.money(totalPaid), "total a pagar sin CI");
    }

    @Test
    void caseB_withGrace(){
        BigDecimal c = new BigDecimal("500.00");
        BigDecimal tea = new BigDecimal("0.20");
        int n = 8;
        int graceTotal = 1;
        int gracePartial = 1;

        BigDecimal teq = calculator.periodicRate(tea);

        String[][] expected = {
                // SI, I, R, A, SF
                {"500.00", "3.81", "0.00", "0.00", "503.81"},
                {"503.81", "3.84", "3.84", "0.00", "503.81"},
                {"503.81", "3.84", "86.22", "82.38", "421.43"},
                {"421.43", "3.21", "86.22", "83.01", "338.42"},
                {"338.42", "2.58", "86.22", "83.64", "254.78"},
                {"254.78", "1.94", "86.22", "84.28", "170.50"},
                {"170.50", "1.30", "86.22", "84.92", "85.57"},
                {"85.57", "0.65", "86.22", "85.57", "0.00"}
        };

        BigDecimal balance = c;
        BigDecimal totalPaid = BigDecimal.ZERO;

        for(int k = 1; k <= n; k++){
            BigDecimal opening = balance;
            String type = k <= graceTotal ? "T" : (k <= graceTotal + gracePartial ? "P" : "S");

            BigDecimal interest = calculator.installmentInterest(opening, teq);
            BigDecimal payment;
            BigDecimal amortization;
            BigDecimal remaining;

            switch(type){
                case "T" -> {
                    payment = BigDecimal.ZERO;
                    amortization = BigDecimal.ZERO;
                    remaining = opening.add(interest);
                }
                case "P" -> {
                    payment = interest;
                    amortization = BigDecimal.ZERO;
                    remaining = opening;
                }
                default -> {
                    payment = calculator.frenchInstallmentPayment(opening, teq, n - k + 1);
                    amortization = calculator.amortization(payment, interest);
                    remaining = opening.subtract(amortization);
                }
            }

            int row = k - 1;
            assertEquals(new BigDecimal(expected[row][0]), calculator.money(opening), "SI k=" + k);
            assertEquals(new BigDecimal(expected[row][1]), calculator.money(interest), "I k=" + k);
            assertEquals(new BigDecimal(expected[row][2]), calculator.money(payment), "R k=" + k);
            assertEquals(new BigDecimal(expected[row][3]), calculator.money(amortization), "A k=" + k);
            assertEquals(new BigDecimal(expected[row][4]), calculator.money(remaining), "SF k=" + k);

            totalPaid = totalPaid.add(payment);
            balance = remaining;
        }

        assertEquals(new BigDecimal("521.19"), calculator.money(totalPaid), "total a pagar sin CI");
    }

    @Test
    void caseC_moratoryInterestOnInstallmentThreeOfCaseB(){
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
}

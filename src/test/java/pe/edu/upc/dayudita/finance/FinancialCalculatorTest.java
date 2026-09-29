package pe.edu.upc.dayudita.finance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.upc.dayudita.finance.application.FinancialCalculator;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialCalculatorTest {

    private FinancialCalculator calculator;

    @BeforeEach
    void setUp(){
        calculator = new FinancialCalculator();
    }

    @Test
    void effectiveRateForDays_FifteenDaysReturnsPositiveRate(){
        BigDecimal rate = calculator.effectiveRateForDays(new BigDecimal("0.24"), 15);

        assertTrue(rate.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void frenchPayment_ReturnsConstantPositivePayment(){
        BigDecimal payment = calculator.frenchPayment(
                new BigDecimal("1000.00"),
                new BigDecimal("0.009003"),
                6
        );

        assertTrue(payment.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void moratoryInterest_WithoutDelayReturnsZero(){
        BigDecimal interest = calculator.moratoryInterest(
                new BigDecimal("100.00"),
                new BigDecimal("0.35"),
                0
        );

        assertEquals(new BigDecimal("0.00"), interest);
    }
}

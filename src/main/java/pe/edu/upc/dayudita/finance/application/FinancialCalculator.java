package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Algoritmo metodo frances vencido ordinario
 */
@Component
public class FinancialCalculator {

    public static final int COMMERCIAL_YEAR_DAYS = 360;
    public static final int PAYMENT_PERIOD_DAYS = 15;

    private static final int RATE_SCALE = 9;
    private static final int INTERNAL_SCALE = 7;
    private static final int MONEY_SCALE = 2;


    public BigDecimal periodicRate(BigDecimal annualEffectiveRate){
        return effectiveRateForDays(annualEffectiveRate, PAYMENT_PERIOD_DAYS);
    }


    public BigDecimal effectiveRateForDays(BigDecimal annualEffectiveRate, int days){
        if(days <= 0){
            return BigDecimal.ZERO.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        }

        double base = BigDecimal.ONE.add(annualEffectiveRate).doubleValue();
        double exponent = (double) days / COMMERCIAL_YEAR_DAYS;
        double result = Math.pow(base, exponent) - 1;

        return BigDecimal.valueOf(result).setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }


    public BigDecimal installmentInterest(BigDecimal openingBalance, BigDecimal periodRate){
        return internal(openingBalance.multiply(periodRate));
    }


    public BigDecimal frenchInstallmentPayment(
            BigDecimal openingBalance,
            BigDecimal periodRate,
            int remainingPeriods
    ){
        double si = openingBalance.doubleValue();
        double i = periodRate.doubleValue();
        double factor = Math.pow(1 + i, remainingPeriods);
        double payment = si * ((i * factor) / (factor - 1));

        return internal(BigDecimal.valueOf(payment));
    }


    public BigDecimal amortization(BigDecimal payment, BigDecimal interest){
        return internal(payment.subtract(interest));
    }


    public BigDecimal moratoryTrancheRate(BigDecimal moratoryAnnualEffectiveRate, int overdueDays){
        if(overdueDays <= 0){
            return BigDecimal.ZERO.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        }

        return effectiveRateForDays(moratoryAnnualEffectiveRate, overdueDays);
    }


    public BigDecimal moratoryInterest(BigDecimal installmentPayment, BigDecimal moratoryTrancheRate){
        if(moratoryTrancheRate.compareTo(BigDecimal.ZERO) == 0){
            return internal(BigDecimal.ZERO);
        }

        return internal(installmentPayment.multiply(moratoryTrancheRate));
    }


    public BigDecimal totalPayment(BigDecimal installmentPayment, BigDecimal moratoryInterest){
        return internal(installmentPayment.add(moratoryInterest));
    }

    private BigDecimal internal(BigDecimal value){
        return value.setScale(INTERNAL_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal money(BigDecimal value){
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}

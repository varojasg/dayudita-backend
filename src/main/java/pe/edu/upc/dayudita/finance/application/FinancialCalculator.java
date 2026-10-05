package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Motor de cálculo del método francés vencido ordinario, quincenal, con
 * periodos de gracia (total/parcial) y mora. Las fórmulas y las escalas de
 * redondeo reproducen exactamente el simulador de Excel que es la fuente de
 * verdad del curso.
 */
@Component
public class FinancialCalculator {

    public static final int COMMERCIAL_YEAR_DAYS = 360;
    public static final int PAYMENT_PERIOD_DAYS = 15;

    private static final int RATE_SCALE = 9;
    private static final int INTERNAL_SCALE = 7;
    private static final int MONEY_SCALE = 2;

    /**
     * TEQ = (1 + teaPactada)^(15/360) - 1, redondeada a 9 decimales.
     */
    public BigDecimal periodicRate(BigDecimal annualEffectiveRate){
        return effectiveRateForDays(annualEffectiveRate, PAYMENT_PERIOD_DAYS);
    }

    /**
     * Tasa efectiva equivalente a la TEA dada para un número de días,
     * sobre año comercial de 360 días: (1+TEA)^(dias/360) - 1.
     * Redondeada a 9 decimales (igual que el TEQ y la tasa moratoria de tramo).
     */
    public BigDecimal effectiveRateForDays(BigDecimal annualEffectiveRate, int days){
        if(days <= 0){
            return BigDecimal.ZERO.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        }

        double base = BigDecimal.ONE.add(annualEffectiveRate).doubleValue();
        double exponent = (double) days / COMMERCIAL_YEAR_DAYS;
        double result = Math.pow(base, exponent) - 1;

        return BigDecimal.valueOf(result).setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * I(k) = redondear7(SI(k) * TEQ)
     */
    public BigDecimal installmentInterest(BigDecimal openingBalance, BigDecimal periodRate){
        return internal(openingBalance.multiply(periodRate));
    }

    /**
     * R(k) para una cuota en tipo S: cuota francesa calculada sobre los
     * periodos que quedan por delante (incluyendo la actual).
     * R = SI * TEQ * (1+TEQ)^m / ((1+TEQ)^m - 1)
     */
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

    /**
     * A(k) = R(k) - I(k)
     */
    public BigDecimal amortization(BigDecimal payment, BigDecimal interest){
        return internal(payment.subtract(interest));
    }

    /**
     * Tasa moratoria del tramo = (1 + teaMoratoriaPactada)^(diasMora/360) - 1,
     * redondeada a 9 decimales. 0 si no hay días de mora.
     */
    public BigDecimal moratoryTrancheRate(BigDecimal moratoryAnnualEffectiveRate, int overdueDays){
        if(overdueDays <= 0){
            return BigDecimal.ZERO.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        }

        return effectiveRateForDays(moratoryAnnualEffectiveRate, overdueDays);
    }

    /**
     * interesMoratorio(k) = redondear7( R(k) * tasaMoratoriaTramo(k) )
     */
    public BigDecimal moratoryInterest(BigDecimal installmentPayment, BigDecimal moratoryTrancheRate){
        if(moratoryTrancheRate.compareTo(BigDecimal.ZERO) == 0){
            return internal(BigDecimal.ZERO);
        }

        return internal(installmentPayment.multiply(moratoryTrancheRate));
    }

    /**
     * pagoTotal(k) = R(k) + interesMoratorio(k)
     */
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

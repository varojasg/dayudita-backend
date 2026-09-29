package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class FinancialCalculator {

    private static final int COMMERCIAL_YEAR_DAYS = 360;
    private static final int RATE_SCALE = 12;

    public BigDecimal effectiveRateForDays(BigDecimal annualEffectiveRate, int days){
        if(days <= 0){
            return BigDecimal.ZERO.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        }

        // Primero, la TEA se traslada al número de días del periodo usando el año comercial de 360 días: TEP = (1 + TEA)^(días/360) - 1.
        double base = BigDecimal.ONE.add(annualEffectiveRate).doubleValue();
        double exponent = (double) days / COMMERCIAL_YEAR_DAYS;
        double result = Math.pow(base, exponent) - 1;

        return BigDecimal.valueOf(result).setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal capitalize(BigDecimal principal, BigDecimal effectiveRate){
        // Luego, si existe un periodo de gracia total, no se paga cuota ni amortización; por eso el interés del periodo se capitaliza: Saldo final = Saldo inicial * (1 + TEP).
        BigDecimal result = principal.multiply(BigDecimal.ONE.add(effectiveRate));
        return money(result);
    }

    public BigDecimal frenchPayment(
            BigDecimal principal,
            BigDecimal periodRate,
            int installmentCount
    ){
        // Después, en el método francés vencido se obtiene una cuota constante: R = C * [TEP * (1 + TEP)^n] / [(1 + TEP)^n - 1].
        double c = principal.doubleValue();
        double i = periodRate.doubleValue();
        double factor = Math.pow(1 + i, installmentCount);
        double payment = c * ((i * factor) / (factor - 1));

        return money(BigDecimal.valueOf(payment));
    }

    public BigDecimal installmentInterest(BigDecimal openingBalance, BigDecimal periodRate){
        // A continuación, el interés de cada cuota se calcula sobre el saldo inicial del periodo: I = TEP * SI.
        return money(openingBalance.multiply(periodRate));
    }

    public BigDecimal installmentAmortization(BigDecimal payment, BigDecimal interest){
        // Por lo tanto, la amortización que reduce el capital es la parte de la cuota que queda después de restar el interés: A = R - I.
        return money(payment.subtract(interest));
    }

    public BigDecimal moratoryInterest(
            BigDecimal overdueAmount,
            BigDecimal moratoryAnnualEffectiveRate,
            int overdueDays
    ){
        if(overdueDays <= 0){
            return money(BigDecimal.ZERO);
        }

        // Finalmente, cuando existe atraso, la tasa moratoria anual se convierte a los días vencidos y se aplica únicamente al importe que permanece pendiente.
        BigDecimal moratoryRate = effectiveRateForDays(moratoryAnnualEffectiveRate, overdueDays);
        return money(overdueAmount.multiply(moratoryRate));
    }

    public BigDecimal money(BigDecimal value){
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

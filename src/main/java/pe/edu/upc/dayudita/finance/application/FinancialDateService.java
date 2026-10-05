package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Fechas sobre año comercial de 360 días (12 meses de 30 días), usando la
 * convención europea de DIAS360: solo el día 31 se ajusta a 30; los días de
 * febrero no se tocan.
 */
@Service
public class FinancialDateService {

    private static final int PAYMENT_PERIOD_DAYS = FinancialCalculator.PAYMENT_PERIOD_DAYS;

    public int commercialDaysBetween(LocalDate startDate, LocalDate endDate){
        if(endDate.isBefore(startDate)){
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la fecha inicial");
        }

        return commercialSerial(endDate) - commercialSerial(startDate);
    }

    /**
     * Primera fecha de corte (dia {@code diaCorte} del mes, pactado con el
     * cliente; si el mes es mas corto, cae en su ultimo dia) igual o
     * posterior a la fecha dada.
     */
    public LocalDate firstCutoffOnOrAfter(LocalDate date, int diaCorte){
        YearMonth month = YearMonth.from(date);
        LocalDate candidate = dateForDay(month, diaCorte);

        if(!date.isAfter(candidate)){
            return candidate;
        }

        return dateForDay(month.plusMonths(1), diaCorte);
    }

    /**
     * Siguiente fecha de corte/pago: exactamente {@value #PAYMENT_PERIOD_DAYS}
     * dias comerciales despues de la anterior (convencion 30/360), sin
     * reenganchar a un dia fijo del calendario.
     */
    public LocalDate nextDueDate(LocalDate previousDueDate){
        return fromCommercialSerial(commercialSerial(previousDueDate) + PAYMENT_PERIOD_DAYS);
    }

    private LocalDate dateForDay(YearMonth month, int contractualDay){
        int realDay = Math.min(contractualDay, month.lengthOfMonth());
        return month.atDay(realDay);
    }

    private int commercialDay(LocalDate date){
        return date.getDayOfMonth() == 31 ? 30 : date.getDayOfMonth();
    }

    private int commercialSerial(LocalDate date){
        return date.getYear() * 360 + (date.getMonthValue() - 1) * 30 + commercialDay(date);
    }

    private LocalDate fromCommercialSerial(int serial){
        int day = ((serial - 1) % 30) + 1;
        int totalMonths = (serial - 1) / 30;
        int year = totalMonths / 12;
        int monthValue = totalMonths % 12 + 1;

        return dateForDay(YearMonth.of(year, monthValue), day);
    }
}

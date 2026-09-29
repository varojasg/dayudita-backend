package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class FinancialDateService {

    public int commercialDaysBetween(LocalDate startDate, LocalDate endDate){
        if(endDate.isBefore(startDate)){
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la fecha inicial");
        }

        int startDay = commercialDay(startDate);
        int endDay = commercialDay(endDate);

        return (endDate.getYear() - startDate.getYear()) * 360
                + (endDate.getMonthValue() - startDate.getMonthValue()) * 30
                + (endDay - startDay);
    }

    public LocalDate getGraceEndDate(
            LocalDate purchaseDate,
            Integer cutoffDay,
            Integer paymentDay
    ){
        LocalDate cutoffDate = getCutoffDate(purchaseDate, cutoffDay);
        return getPaymentDateForCutoff(cutoffDate, cutoffDay, paymentDay);
    }

    public LocalDate getCutoffDate(LocalDate purchaseDate, Integer cutoffDay){
        YearMonth month = YearMonth.from(purchaseDate);

        if(commercialDay(purchaseDate) <= cutoffDay){
            return dateForDay(month, cutoffDay);
        }

        return dateForDay(month.plusMonths(1), cutoffDay);
    }

    public LocalDate getPaymentDateForCutoff(
            LocalDate cutoffDate,
            Integer cutoffDay,
            Integer paymentDay
    ){
        YearMonth cutoffMonth = YearMonth.from(cutoffDate);

        if(paymentDay > cutoffDay){
            return dateForDay(cutoffMonth, paymentDay);
        }

        return dateForDay(cutoffMonth.plusMonths(1), paymentDay);
    }

    public LocalDate addPaymentPeriods(LocalDate baseDate, int periods, int periodDays){
        if(periods < 0 || periodDays <= 0){
            throw new IllegalArgumentException("El periodo de pago no es valido");
        }

        int commercialIndex = baseDate.getYear() * 360
                + (baseDate.getMonthValue() - 1) * 30
                + (commercialDay(baseDate) - 1)
                + periods * periodDays;

        int year = Math.floorDiv(commercialIndex, 360);
        int dayOfYear = Math.floorMod(commercialIndex, 360);
        int month = dayOfYear / 30 + 1;
        int day = dayOfYear % 30 + 1;

        return dateForDay(YearMonth.of(year, month), day);
    }

    public boolean matchesContractualDay(LocalDate date, Integer contractualDay){
        return date.equals(dateForDay(YearMonth.from(date), contractualDay));
    }

    private LocalDate dateForDay(YearMonth month, Integer contractualDay){
        int realDay = Math.min(contractualDay, month.lengthOfMonth());
        return month.atDay(realDay);
    }

    private int commercialDay(LocalDate date){
        if(date.getMonthValue() == 2 && date.getDayOfMonth() == date.lengthOfMonth()){
            return 30;
        }

        return Math.min(date.getDayOfMonth(), 30);
    }
}

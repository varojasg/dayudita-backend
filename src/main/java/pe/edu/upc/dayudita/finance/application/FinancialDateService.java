package pe.edu.upc.dayudita.finance.application;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;


@Service
public class FinancialDateService {

    private static final int PAYMENT_PERIOD_DAYS = FinancialCalculator.PAYMENT_PERIOD_DAYS;

    public int commercialDaysBetween(LocalDate startDate, LocalDate endDate){
        if(endDate.isBefore(startDate)){
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la fecha inicial");
        }

        return commercialSerial(endDate) - commercialSerial(startDate);
    }

    public LocalDate dueDateForPeriod(LocalDate purchaseDate, int period){
        return fromCommercialSerial(commercialSerial(purchaseDate) + PAYMENT_PERIOD_DAYS * period);
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

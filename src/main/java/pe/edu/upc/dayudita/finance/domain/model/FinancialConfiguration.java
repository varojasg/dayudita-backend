package pe.edu.upc.dayudita.finance.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "financial_configuration")
@Getter
@Setter
@NoArgsConstructor
public class FinancialConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal minAnnualEffectiveRate;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal maxAnnualEffectiveRate;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal annualEffectiveRate;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal moratoryAnnualEffectiveRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minCapital;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal maxCapital;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Column(nullable = false)
    private Integer maxInstallments;
}

package pe.edu.upc.dayudita.finance.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.dayudita.sales.domain.model.Purchase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_plans")
@Getter
@Setter
@NoArgsConstructor
public class CreditPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "purchase_id", nullable = false, unique = true)
    private Purchase purchase;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal principal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal capitalizedPrincipal;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal annualEffectiveRate;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal periodRate;

    @Column(nullable = false)
    private Integer graceDays;

    @Column(nullable = false)
    private Integer installmentCount;

    @Column(nullable = false)
    private LocalDate graceEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditPlanStatus status = CreditPlanStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

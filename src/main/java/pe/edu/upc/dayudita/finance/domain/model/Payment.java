package pe.edu.upc.dayudita.finance.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentType type;

    @ManyToOne
    @JoinColumn(name = "statement_id")
    private AccountStatement statement;

    @ManyToOne
    @JoinColumn(name = "installment_id")
    private Installment installment;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal moratoryAnnualEffectiveRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal compensatoryInterest;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal moratoryInterest;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

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

    @ManyToOne
    @JoinColumn(name = "installment_id", nullable = false)
    private Installment installment;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal teaMoratoriaPactada;

    @Column(nullable = false)
    private Integer diasMora;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amortizacion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal interes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal interesMoratorio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

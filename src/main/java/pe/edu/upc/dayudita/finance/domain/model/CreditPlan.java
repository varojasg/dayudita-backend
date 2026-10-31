package pe.edu.upc.dayudita.finance.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.dayudita.sales.domain.model.Purchase;

import java.math.BigDecimal;
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
    private BigDecimal ventaPrecio;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal porcentajeCuotaInicial;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cuotaInicial;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal principal;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal teaPactada;

    @Column(nullable = false, precision = 16, scale = 9)
    private BigDecimal teq;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal teaMoratoriaPactada;

    @Column(nullable = false)
    private Integer numeroMeses;

    /** Numero total de periodos (meses x 2), incluida la gracia total si la hay. */
    @Column(nullable = false)
    private Integer installmentCount;

    /**
     * Dias de gracia total: 15 si el periodo 1 es de gracia, 0 si la tienda
     * no la otorga. Dato informativo: el interes de la fila T usa la TEQ.
     */
    @Column(nullable = false)
    private Integer diasGraciaTotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalIntereses;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal interesCapitalizado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmortizacion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCuotas;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAPagar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreditPlanStatus status = CreditPlanStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

package pe.edu.upc.dayudita.finance.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.dayudita.stores.domain.model.Store;

import java.math.BigDecimal;

/**
 * Política de crédito de una tienda (método francés con gracia).
 * Es la política comercial, no una configuración global del sistema.
 */
@Entity
@Table(name = "financial_configuration")
@Getter
@Setter
@NoArgsConstructor
public class FinancialConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "store_id", nullable = false, unique = true)
    private Store store;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal capitalMinimo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal capitalMaximo;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal teaMinima;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal teaMaxima;

    @Column(nullable = false)
    private Integer plazoMaximoMeses;

    /**
     * Si la tienda aplica el periodo de gracia total automatico (de la fecha
     * de compra a la siguiente fecha de corte, 15 o 30 de cada mes).
     */
    @Column(nullable = false)
    private Boolean otorgaGracia;
}

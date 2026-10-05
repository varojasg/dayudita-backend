package pe.edu.upc.dayudita.clients.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.dayudita.stores.domain.model.Store;

import java.math.BigDecimal;

@Entity
@Table(
        name = "client_accounts",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"client_id", "store_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class ClientAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal teaPactada;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal teaMoratoriaPactada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CreditCurrency currency = CreditCurrency.PEN;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal limiteCredito;

    @Column(nullable = false)
    private Integer plazoMaximoMeses;

    /**
     * Dia del mes (1-31) en el que se pacta el corte del credito de este
     * cliente. Ancla la gracia inicial y, desde ahi, las cuotas siguientes
     * avanzan +15 dias comerciales exactos (sin reenganchar a un dia fijo
     * del calendario).
     */
    @Column(nullable = false)
    private Integer diaCorte;

    @Column(nullable = false)
    private Boolean active = true;
}

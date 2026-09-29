package pe.edu.upc.dayudita.clients.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.dayudita.stores.domain.model.Store;

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

    @Column(nullable = false)
    private Integer cutoffDay;

    @Column(nullable = false)
    private Integer paymentDay;

    @Column(nullable = false)
    private Boolean active = true;
}

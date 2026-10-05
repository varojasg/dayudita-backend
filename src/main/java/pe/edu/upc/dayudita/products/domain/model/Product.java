package pe.edu.upc.dayudita.products.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.dayudita.stores.domain.model.Store;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String supplier;

    @Column(length = 100)
    private String brand;

    @Column(length = 300)
    private String description;

    @Column(length = 50)
    private String unitOfMeasure;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cashPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal creditPrice;

    @Column(nullable = false)
    private Boolean allowsInstallments;

    @Column(nullable = false)
    private Boolean active = true;
}

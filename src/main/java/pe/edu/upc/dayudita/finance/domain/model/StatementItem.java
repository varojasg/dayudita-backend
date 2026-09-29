package pe.edu.upc.dayudita.finance.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.dayudita.sales.domain.model.Purchase;

import java.math.BigDecimal;

@Entity
@Table(name = "statement_items")
@Getter
@Setter
@NoArgsConstructor
public class StatementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "statement_id", nullable = false)
    private AccountStatement statement;

    @OneToOne
    @JoinColumn(name = "purchase_id", nullable = false, unique = true)
    private Purchase purchase;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal principal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal compensatoryInterest;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;
}

package pe.edu.upc.dayudita.finance.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.dayudita.clients.domain.model.ClientAccount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "account_statements",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"client_account_id", "cutoff_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AccountStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_account_id", nullable = false)
    private ClientAccount clientAccount;

    @Column(name = "cutoff_date", nullable = false)
    private LocalDate cutoffDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 16, scale = 12)
    private BigDecimal annualEffectiveRate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal principal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal compensatoryInterest;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatementStatus status = StatementStatus.OPEN;

    private LocalDate paymentDate;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

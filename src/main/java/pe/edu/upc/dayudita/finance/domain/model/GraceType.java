package pe.edu.upc.dayudita.finance.domain.model;

/**
 * Tipo de cuota dentro del cronograma del método francés:
 * T = gracia total, solo en el periodo 1 (no se paga, el interés a la TEQ se capitaliza),
 * S = cuota estándar (amortiza capital).
 */
public enum GraceType {
    TOTAL,
    STANDARD
}

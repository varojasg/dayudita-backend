package pe.edu.upc.dayudita.finance.domain.model;

/**
 * Tipo de cuota dentro del cronograma del método francés con gracia por
 * fecha de corte:
 * T = gracia total (no se paga, el interés se capitaliza),
 * S = cuota estándar (amortiza capital).
 */
public enum GraceType {
    TOTAL,
    STANDARD
}

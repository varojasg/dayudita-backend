package pe.edu.upc.dayudita.finance.domain.model;

/**
 * Estado persistido de una cuota. Los estados "Gracia total" y "Vencida"
 * que se muestran al usuario son derivados (tipo T, o PENDING con fecha de
 * vencimiento pasada) y se calculan al construir la respuesta
 */
public enum InstallmentStatus {
    PENDING,
    PAID
}

package pe.edu.upc.dayudita.finance.interfaces.rest.dto;

/**
 * Estado visible de una cuota (secc. 1.5 del prompt). Se calcula al
 * construir la respuesta -- no se persiste -- porque "Vencida" depende de
 * la fecha de hoy mientras la cuota no esta pagada.
 */
public enum InstallmentDisplayStatus {
    GRACE_TOTAL,
    PENDING,
    PAID,
    OVERDUE
}

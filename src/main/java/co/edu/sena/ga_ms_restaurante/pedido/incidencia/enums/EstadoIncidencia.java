package co.edu.sena.ga_ms_restaurante.pedido.incidencia.enums;

/**
 * Estado de una incidencia (cancelación o devolución) registrada en Restaurante.
 *
 * EN_PROCESO → se acaba de registrar (p. ej. el ítem quedó EN_DEVOLUCION esperando
 *              que Cocina/Bar lo reprocesen).
 * RESUELTA   → Cocina/Bar confirmaron que el reproceso terminó (o la cancelación
 *              quedó aplicada sin más trámite).
 */
public enum EstadoIncidencia {
    EN_PROCESO,
    RESUELTA
}
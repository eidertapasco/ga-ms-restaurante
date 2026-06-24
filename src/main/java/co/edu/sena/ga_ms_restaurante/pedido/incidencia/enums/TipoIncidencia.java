package co.edu.sena.ga_ms_restaurante.pedido.incidencia.enums;

/**
 * Tipo de incidencia registrada sobre un pedido o un ítem puntual.
 * Mismo concepto que TipoIncidencia en Cocina/Bar, pero esta es la copia
 * propia de Restaurante — cada microservicio persiste lo que necesita
 * para su propio dominio.
 */
public enum TipoIncidencia {
    CANCELACION,
    DEVOLUCION
}
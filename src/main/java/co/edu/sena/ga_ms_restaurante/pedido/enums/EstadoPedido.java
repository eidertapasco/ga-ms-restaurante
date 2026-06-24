package co.edu.sena.ga_ms_restaurante.pedido.enums;

public enum EstadoPedido {
    BORRADOR,
    ENVIADO_COCINA,
    EN_PREPARACION,
    LISTO_PARA_SERVIR,
    ENTREGADO,
    /**
     * NUEVO — al menos un ítem del pedido fue devuelto y Cocina/Bar lo está
     * reprocesando. Antes de este cambio, devolverGlobal() usaba CANCELADO
     * por no existir este valor. */
    EN_DEVOLUCION,
    FACTURADO,
    CANCELADO
}
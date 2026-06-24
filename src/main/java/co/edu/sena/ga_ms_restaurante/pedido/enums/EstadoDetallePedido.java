package co.edu.sena.ga_ms_restaurante.pedido.enums;

/** Estado de un ítem del pedido. Antes vivía como String libre en DetallePedido.estadoDetalle. */
public enum EstadoDetallePedido {
    PENDIENTE,
    PREPARANDO,
    TERMINADO,
    EN_DEVOLUCION,   // nuevo — se usa desde feature/reglas-cancelacion-devolucion
    ENTREGADO,       // nuevo — idem
    CANCELADO,
    DEVUELTO
}
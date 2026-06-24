package co.edu.sena.ga_ms_restaurante.pedido.dto.response;

import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoDetallePedido;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DetallePedidoResponse {

    private UUID id;
    private String productoId;
    private String nombreProducto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotalLinea;
    //private String categoria; solo si el frontend requiere que le devuelva la categoria tambien
    private String observaciones;

    // Estado individual del ítem según Cocina/Bar.
    // Antes era String libre; ahora es el enum EstadoDetallePedido.
    // Jackson lo serializa igual que antes (como string en el JSON: "CANCELADO", "LISTO_PARA_SERVIR", etc.)
    private EstadoDetallePedido estadoDetalle;
}
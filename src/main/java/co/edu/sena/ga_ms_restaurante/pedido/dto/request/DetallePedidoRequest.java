package co.edu.sena.ga_ms_restaurante.pedido.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetallePedidoRequest {

    @NotBlank(message = "El ID del producto es obligatorio")
    private String productoId;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombreProducto;

    @Min(value = 1, message = "La cantidad mínima es 1")
    private int cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precioUnitario;

    // Categoría usada internamente para separar eventos RabbitMQ
    // COMIDA o BEBIDA — viene del catálogo de productos
    @NotBlank(message = "La categoría del producto es obligatoria")
    private String categoria;

    private String observaciones;
}

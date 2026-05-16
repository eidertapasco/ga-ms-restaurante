package co.edu.sena.ga_ms_restaurante.caja.dto.request;

import co.edu.sena.ga_ms_restaurante.caja.enums.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class FacturarPedidoRequest {

    @NotNull(message = "El ID del pedido es obligatorio")
    private UUID pedidoId;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @NotNull(message = "La propina es obligatoria (puede ser 0)")
    @DecimalMin(value = "0.0", inclusive = true, message = "La propina no puede ser negativa")
    private BigDecimal propina;
}
package co.edu.sena.ga_ms_restaurante.pedido.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PedidoCreateRequest {

    @NotNull(message = "El ID de la mesa es obligatorio")
    private UUID mesaId;

    @Min(value = 1, message = "Debe haber al menos 1 comensal")
    @Max(value = 50, message = "Máximo 50 comensales")
    private int numeroComensales;

    private String notas;

    @NotEmpty(message = "El pedido debe tener al menos un ítem")
    @Valid
    private List<DetallePedidoRequest> detalles;
}


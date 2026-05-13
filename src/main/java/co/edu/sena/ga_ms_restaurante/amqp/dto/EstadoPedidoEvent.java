package co.edu.sena.ga_ms_restaurante.amqp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoPedidoEvent {
    private UUID idPedido;
    private String nuevoEstado;  // "EN_PREPARACION" | "LISTO_PARA_SERVIR" | "CANCELADO"
    private String modulo;       // "COCINA" | "BAR"
}

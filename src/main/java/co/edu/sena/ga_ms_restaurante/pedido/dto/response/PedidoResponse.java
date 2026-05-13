package co.edu.sena.ga_ms_restaurante.pedido.dto.response;

import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PedidoResponse {

    private UUID id;
    private UUID mesaId;
    private String nombreMesa;
    private UUID meseroId;
    private int numeroComensales;
    private String notas;
    private EstadoPedido estado;
    private BigDecimal subtotal;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCierre;
    private List<DetallePedidoResponse> detalles;
}

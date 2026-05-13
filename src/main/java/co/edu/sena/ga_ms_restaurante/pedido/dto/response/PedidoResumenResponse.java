package co.edu.sena.ga_ms_restaurante.pedido.dto.response;

import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PedidoResumenResponse {

    private UUID id;
    private String nombreMesa;
    private UUID meseroId;
    private int numeroComensales;
    private EstadoPedido estado;
    private BigDecimal subtotal;
    private LocalDateTime fechaCreacion;
}
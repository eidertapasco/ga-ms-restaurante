package co.edu.sena.ga_ms_restaurante.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class PedidoResponseDTO {

    private Long idPedido;
    private Long idMesa;
    private Long idMesero;
    private BigDecimal totalFinal;
    private String estado;
    private Instant createdAt;

    private List<DetallePedidoResponseDTO> detalles;
}

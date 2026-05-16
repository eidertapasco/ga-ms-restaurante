package co.edu.sena.ga_ms_restaurante.reporte.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PedidoPorMesaResponse {

    private UUID mesaId;
    private String nombreMesa;
    private long totalPedidos;
    private BigDecimal totalFacturado;
}

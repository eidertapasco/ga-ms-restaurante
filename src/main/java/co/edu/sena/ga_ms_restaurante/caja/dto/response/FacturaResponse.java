package co.edu.sena.ga_ms_restaurante.caja.dto.response;

import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoFactura;
import co.edu.sena.ga_ms_restaurante.caja.enums.MetodoPago;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FacturaResponse {

    private UUID id;
    private String numeroFactura;
    private UUID pedidoId;
    private String nombreMesa;
    private UUID sesionCajaId;
    private UUID cajeroId;
    private BigDecimal subtotal;
    private BigDecimal propina;
    private BigDecimal total;
    private MetodoPago metodoPago;
    private EstadoFactura estado;
    private LocalDateTime fechaEmision;
}

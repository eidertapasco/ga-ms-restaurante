package co.edu.sena.ga_ms_restaurante.caja.dto.response;

import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoSesion;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SesionCajaResponse {

    private UUID id;
    private UUID cajeroId;
    private EstadoSesion estado;
    private BigDecimal baseEfectivo;
    private BigDecimal totalVentasEfectivo;
    private BigDecimal totalVentasTarjeta;
    private BigDecimal totalVentasTransferencia;
    private BigDecimal efectivoReal;
    private BigDecimal diferencia;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
}

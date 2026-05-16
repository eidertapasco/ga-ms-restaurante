package co.edu.sena.ga_ms_restaurante.reporte.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ResumenVentasResponse {

    private long totalPedidosFacturados;
    private long totalPedidosCancelados;
    private BigDecimal totalVentasBrutas;    // suma de subtotales
    private BigDecimal totalPropinas;
    private BigDecimal totalVentasConPropina;
    private BigDecimal ventasEfectivo;
    private BigDecimal ventasTarjeta;
    private BigDecimal ventasTransferencia;
    private BigDecimal ventasCortesia;
}

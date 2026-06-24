package co.edu.sena.ga_ms_restaurante.pedido.incidencia.dto.response;

import co.edu.sena.ga_ms_restaurante.pedido.incidencia.enums.EstadoIncidencia;
import co.edu.sena.ga_ms_restaurante.pedido.incidencia.enums.TipoIncidencia;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representación de una incidencia (cancelación/devolución) dentro del JSON
 * de PedidoResponse.incidencias.
 */
@Data
public class IncidenciaPedidoResponse {

    private UUID id;
    private TipoIncidencia tipo;
    private UUID detalleId;          // null = incidencia global del pedido
    private String producto;         // null si es global
    private Integer cantidadAfectada;
    private String motivo;
    private EstadoIncidencia estado;
    private UUID registradaPor;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaResolucion;
}
package co.edu.sena.ga_ms_restaurante.amqp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de salida — formato exacto que los módulos Cocina y Bar/Barismo
 * esperan recibir. Solo existe en la capa amqp; nunca entra al dominio.
 *
 * Regla 2 aplicada: campo 'prioridad' no se incluye.
 * Regla 3 aplicada: 'nombreMesero' se mockea en el adapter.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComandaRequestDTO {

    private UUID          idPedidoRestaurante;
    private UUID          idMesero;
    private String        nombreMesero;
    private UUID          idMesa;
    private Integer       numeroMesa;
    private LocalDateTime fechaPedido;
    private String        notasAdicionales;
    // prioridad: OMITIDA — rechazada por diseño (Regla 2)

    private List<DetalleComandaDto> detalles;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetalleComandaDto {
        private String  idDetallePedido;    // UUID del DetallePedido en Restaurante
        private String  idReceta;           // = idProducto en la nomenclatura de Restaurante
        private Integer cantidad;
        private String  observacionesPlato; // = observaciones en la nomenclatura de Restaurante
    }
}
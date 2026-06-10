package co.edu.sena.ga_ms_restaurante.amqp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento que Restaurante RECIBE desde Cocina o Bar cuando el estado
 * de un plato o bebida individual cambia.
 *
 * Cola que escucha: restaurante.plato.estado
 * Routing Key     : pedido.plato.estado
 *
 * idDetallePedido puede ser null si Cocina/Bar aún no lo envían
 * (se maneja graciosamente en el listener sin lanzar error).
 *
 * Valores esperados en estadoPlato: "PREPARANDO" | "TERMINADO" | "LISTO"
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionPlatoEvent {

    private UUID idPedidoRestaurante;   // identifica el Pedido en nuestra BD

    private UUID idDetallePedido;       // nullable — identifica el DetallePedido exacto

    private Integer numeroMesa;         // informativo (número entero)

    private String nombrePlato;         // nombre del plato o bebida

    private String estadoPlato;         // "PREPARANDO" | "TERMINADO" | "LISTO"

    private LocalDateTime fechaNotificacion;
}
package co.edu.sena.ga_ms_restaurante.amqp.listener;

import co.edu.sena.ga_ms_restaurante.amqp.dto.NotificacionPlatoEvent;
import co.edu.sena.ga_ms_restaurante.config.amqp.RabbitMQConfig;
import co.edu.sena.ga_ms_restaurante.pedido.model.DetallePedido;
import co.edu.sena.ga_ms_restaurante.pedido.repository.DetallePedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstadoPlatoListener {

    private final DetallePedidoRepository detallePedidoRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PLATO_ESTADO)
    public void onEstadoPlatoActualizado(NotificacionPlatoEvent evento) {
        log.info("Notificación por plato recibida — pedido: {}, detalle: {}, estado: '{}'",
                evento.getIdPedidoRestaurante(),
                evento.getIdDetallePedido() != null ? evento.getIdDetallePedido() : "SIN_ID",
                evento.getEstadoPlato());

        try {
            if (evento.getIdDetallePedido() == null) {
                /*
                 * Sin idDetallePedido no es posible identificar cuál ítem actualizar.
                 * Ejemplo: si el pedido tiene dos "Bandeja Paisa" y llega la notificación
                 * "Bandeja Paisa está lista", no hay forma de saber cuál de las dos es.
                 * Nuestro lado está listo; en cuanto Cocina/Bar envíen este campo,
                 * el update ocurrirá automáticamente sin cambios adicionales aquí.
                 */
                log.warn("Notificación de plato '{}' recibida sin idDetallePedido " +
                                "para pedido {} — no se puede actualizar el ítem específico.",
                        evento.getNombrePlato(), evento.getIdPedidoRestaurante());
                return;
            }

            Optional<DetallePedido> detalleOpt =
                    detallePedidoRepository.findById(evento.getIdDetallePedido());

            if (detalleOpt.isEmpty()) {
                log.warn("Ítem {} no encontrado al procesar notificación de plato — evento ignorado",
                        evento.getIdDetallePedido());
                return;
            }

            DetallePedido detalle = detalleOpt.get();
            String nuevoEstado = traducirEstadoPlato(evento.getEstadoPlato());
            detalle.setEstadoDetalle(nuevoEstado);
            detallePedidoRepository.save(detalle);

            log.info("Ítem {} → estadoDetalle actualizado a '{}'",
                    evento.getIdDetallePedido(), nuevoEstado);

        } catch (Exception e) {
            log.error("Error procesando notificación de plato — pedido: {}, error: {}",
                    evento.getIdPedidoRestaurante(), e.getMessage());
            // No relanzar — evita reencolas infinitas en RabbitMQ
        }
    }

    /**
     * Normaliza los estados que pueden enviar Cocina o Bar al formato
     * que usa el campo estadoDetalle de DetallePedido.
     */
    private String traducirEstadoPlato(String estadoRaw) {
        if (estadoRaw == null) return "PENDIENTE";
        return switch (estadoRaw.toUpperCase()) {
            case "PREPARANDO" -> "PREPARANDO";
            case "TERMINADO"  -> "TERMINADO";
            case "LISTO"      -> "TERMINADO"; // alias — Bar puede enviar "LISTO"
            case "CANCELADO"  -> "CANCELADO";
            default           -> estadoRaw.toUpperCase();
        };
    }
}
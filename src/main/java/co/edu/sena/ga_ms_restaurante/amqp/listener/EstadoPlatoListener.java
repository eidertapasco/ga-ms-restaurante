package co.edu.sena.ga_ms_restaurante.amqp.listener;

import co.edu.sena.ga_ms_restaurante.amqp.dto.NotificacionPlatoEvent;
import co.edu.sena.ga_ms_restaurante.config.amqp.RabbitMQConfig;
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoDetallePedido;
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
        log.info("Notificación por plato — pedido: {}, detalle: {}, estado: '{}'",
                evento.getIdPedidoRestaurante(),
                evento.getIdDetallePedido() != null ? evento.getIdDetallePedido() : "SIN_ID",
                evento.getEstadoPlato());

        try {
            if (evento.getIdDetallePedido() == null) {
                // Sin idDetallePedido no se puede saber a cuál ítem corresponde (ver caso de 2 "Bandeja Paisa" en el mismo pedido).
                log.warn("Notificación de plato '{}' sin idDetallePedido — pedido {} — ignorada",
                        evento.getNombrePlato(), evento.getIdPedidoRestaurante());
                return;
            }

            Optional<DetallePedido> detalleOpt =
                    detallePedidoRepository.findById(evento.getIdDetallePedido());

            if (detalleOpt.isEmpty()) {
                log.warn("Ítem {} no encontrado — evento ignorado", evento.getIdDetallePedido());
                return;
            }

            DetallePedido detalle = detalleOpt.get();
            EstadoDetallePedido nuevoEstado = traducirEstadoPlato(evento.getEstadoPlato());
            detalle.setEstadoDetalle(nuevoEstado);
            detallePedidoRepository.save(detalle);

            log.info("Ítem {} → estadoDetalle '{}'", evento.getIdDetallePedido(), nuevoEstado);

        } catch (Exception e) {
            log.error("Error procesando notificación de plato — pedido: {}, error: {}",
                    evento.getIdPedidoRestaurante(), e.getMessage());
            // No relanzar — evita reencolas infinitas en RabbitMQ
        }
    }

    private EstadoDetallePedido traducirEstadoPlato(String estadoRaw) {
        if (estadoRaw == null) return EstadoDetallePedido.PENDIENTE;
        return switch (estadoRaw.toUpperCase()) {
            case "PREPARANDO" -> EstadoDetallePedido.PREPARANDO;
            case "TERMINADO", "LISTO" -> EstadoDetallePedido.TERMINADO; // Bar puede enviar "LISTO"
            case "CANCELADO" -> EstadoDetallePedido.CANCELADO;
            default -> {
                log.warn("Estado de plato desconocido: '{}' — se deja en PENDIENTE", estadoRaw);
                yield EstadoDetallePedido.PENDIENTE;
            }
        };
    }
}
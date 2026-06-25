package co.edu.sena.ga_ms_restaurante.amqp.listener;

import co.edu.sena.ga_ms_restaurante.amqp.dto.NotificacionPlatoEvent;
import co.edu.sena.ga_ms_restaurante.config.amqp.RabbitMQConfig;
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoDetallePedido;
import co.edu.sena.ga_ms_restaurante.pedido.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstadoPlatoListener {

    private final PedidoService pedidoService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PLATO_ESTADO)
    public void onEstadoPlatoActualizado(NotificacionPlatoEvent evento) {
        log.info("Notificación por plato — pedido: {}, detalle: {}, estado: '{}'",
                evento.getIdPedidoRestaurante(),
                evento.getIdDetallePedido() != null ? evento.getIdDetallePedido() : "SIN_ID",
                evento.getEstadoPlato());

        try {
            if (evento.getIdDetallePedido() == null) {
                log.warn("Notificación de plato '{}' sin idDetallePedido — pedido {} — ignorada",
                        evento.getNombrePlato(), evento.getIdPedidoRestaurante());
                return;
            }

            EstadoDetallePedido nuevoEstado = traducirEstadoPlato(evento.getEstadoPlato());
            pedidoService.actualizarEstadoDetalleDesdeEvento(evento.getIdDetallePedido(), nuevoEstado);

        } catch (Exception e) {
            log.error("Error procesando notificación de plato — pedido: {}, error: {}",
                    evento.getIdPedidoRestaurante(), e.getMessage());
        }
    }

    private EstadoDetallePedido traducirEstadoPlato(String estadoRaw) {
        if (estadoRaw == null) return EstadoDetallePedido.PENDIENTE;
        return switch (estadoRaw.toUpperCase()) {
            case "PREPARANDO" -> EstadoDetallePedido.PREPARANDO;
            case "TERMINADO", "LISTO" -> EstadoDetallePedido.TERMINADO;
            case "CANCELADO" -> EstadoDetallePedido.CANCELADO;
            default -> {
                log.warn("Estado de plato desconocido: '{}' — se deja en PENDIENTE", estadoRaw);
                yield EstadoDetallePedido.PENDIENTE;
            }
        };
    }
}
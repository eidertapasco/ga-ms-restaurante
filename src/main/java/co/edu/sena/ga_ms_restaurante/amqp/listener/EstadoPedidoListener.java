package co.edu.sena.ga_ms_restaurante.amqp.listener;

import co.edu.sena.ga_ms_restaurante.amqp.dto.EstadoPedidoEvent;
import co.edu.sena.ga_ms_restaurante.config.amqp.RabbitMQConfig;
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.pedido.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstadoPedidoListener {

    private final PedidoService pedidoService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ESTADO_PEDIDO)
    public void onEstadoActualizado(EstadoPedidoEvent evento) {
        log.info("Evento recibido de {} — pedido: {} → {}",
                evento.getModulo(), evento.getIdPedido(), evento.getNuevoEstado());

        try {
            EstadoPedido nuevoEstado = EstadoPedido.valueOf(evento.getNuevoEstado());
            pedidoService.actualizarEstadoDesdeEvento(evento.getIdPedido(), nuevoEstado);
        } catch (IllegalArgumentException e) {
            log.error("Estado desconocido recibido: '{}' — evento ignorado", evento.getNuevoEstado());
        } catch (Exception e) {
            log.error("Error procesando evento de estado para pedido {}: {}",
                    evento.getIdPedido(), e.getMessage());
            // No relanzar — si se relanza, RabbitMQ reencola indefinidamente
        }
    }
}

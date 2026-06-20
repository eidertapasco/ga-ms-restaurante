package co.edu.sena.ga_ms_restaurante.amqp.publisher;

import co.edu.sena.ga_ms_restaurante.amqp.dto.CancelacionEvent;
import co.edu.sena.ga_ms_restaurante.config.amqp.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica eventos de cancelación/devolución hacia Cocina y Bar.
 *
 * Cada módulo tiene su propia cola y routing key, de modo que una
 * cancelación solo llega al módulo al que le corresponde:
 *   COMIDA → pedido.cancelacion.cocina
 *   BEBIDA → pedido.cancelacion.bar
 *
 * El campo idPlatoEspecifico del evento distingue global (null) de ítem (UUID);
 * no afecta a qué cola se envía, eso lo decide la routing key.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CancelacionPublisher {

    private final RabbitTemplate rabbitTemplate;

    /** Publica la cancelación/devolución hacia la cola de Cocina. */
    public void publicarACocina(CancelacionEvent evento) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_CANCELACION_COCINA, evento);
        log.info("Cancelación → COCINA — pedido: {}, ítem: {}, devolucion: {}",
                evento.getIdComanda(),
                evento.getIdPlatoEspecifico() != null ? evento.getIdPlatoEspecifico() : "GLOBAL",
                evento.isDevolucion());
    }

    /** Publica la cancelación/devolución hacia la cola de Bar/Barismo. */
    public void publicarABar(CancelacionEvent evento) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_CANCELACION_BAR, evento);
        log.info("Cancelación → BAR — pedido: {}, ítem: {}, devolucion: {}",
                evento.getIdComanda(),
                evento.getIdPlatoEspecifico() != null ? evento.getIdPlatoEspecifico() : "GLOBAL",
                evento.isDevolucion());
    }
}
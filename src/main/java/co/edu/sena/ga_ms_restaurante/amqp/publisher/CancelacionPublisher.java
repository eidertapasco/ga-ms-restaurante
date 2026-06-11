package co.edu.sena.ga_ms_restaurante.amqp.publisher;

import co.edu.sena.ga_ms_restaurante.amqp.dto.CancelacionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelacionPublisher {

    private final RabbitTemplate rabbitTemplate;

    public static final String EXCHANGE    = "gastrosena.pedidos";
    public static final String ROUTING_KEY = "pedido.cancelacion";

    public void publicar(CancelacionEvent evento) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, evento);
        log.info("Cancelación publicada — pedido: {}, ítem: {}, devolucion: {}",
                evento.getIdComanda(),
                evento.getIdPlatoEspecifico() != null ? evento.getIdPlatoEspecifico() : "GLOBAL",
                evento.isDevolucion());
    }
}
package co.edu.sena.ga_ms_restaurante.amqp.publisher;

import co.edu.sena.ga_ms_restaurante.amqp.dto.PedidoCocinaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoCocinaPublisher {

    private final RabbitTemplate rabbitTemplate;

    // Constantes — deben coincidir con RabbitMQConfig cuando lo creemos
    public static final String EXCHANGE   = "gastrosena.pedidos";
    public static final String ROUTING_KEY = "pedido.cocina";

    public void publicar(PedidoCocinaEvent evento) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, evento);
        log.info("Evento publicado a Cocina — pedido: {}", evento.getIdPedido());
    }
}

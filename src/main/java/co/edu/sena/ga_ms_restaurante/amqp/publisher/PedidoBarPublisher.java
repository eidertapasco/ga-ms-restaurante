package co.edu.sena.ga_ms_restaurante.amqp.publisher;

import co.edu.sena.ga_ms_restaurante.amqp.dto.PedidoBarEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoBarPublisher {

    private final RabbitTemplate rabbitTemplate;

    public static final String EXCHANGE    = "gastrosena.pedidos";
    public static final String ROUTING_KEY = "pedido.bar";

    public void publicar(PedidoBarEvent evento) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, evento);
        log.info("Evento publicado a Bar — pedido: {}", evento.getIdPedido());
    }
}

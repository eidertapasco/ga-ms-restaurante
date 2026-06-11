package co.edu.sena.ga_ms_restaurante.amqp.publisher;

import co.edu.sena.ga_ms_restaurante.amqp.dto.PedidoBarEvent;
import co.edu.sena.ga_ms_restaurante.pedido.model.Pedido;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoBarPublisher {

    private final RabbitTemplate     rabbitTemplate;
    private final ComandaEventAdapter adapter;

    public static final String EXCHANGE    = "gastrosena.pedidos";
    public static final String ROUTING_KEY = "pedido.bar";

    /**
     * Traduce los ítems de bar a ComandaRequestDTO y publica en el exchange.
     *
     * @param pedido entidad completa — fuente de idMesero, idMesa, etc.
     * @param items  ítems ya filtrados por categoría BEBIDA
     */
    public void publicar(Pedido pedido, List<PedidoBarEvent.Item> items) {
        var dto = adapter.traducirParaBar(pedido, items);
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, dto);
        log.info("Evento publicado a Bar — pedido: {}, ítems: {}",
                pedido.getId(), items.size());
    }
}
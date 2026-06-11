package co.edu.sena.ga_ms_restaurante.config.amqp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Nombres ───────────────────────────────────────────────────────────────
    public static final String EXCHANGE = "gastrosena.pedidos";

    // Colas existentes
    public static final String QUEUE_COCINA        = "restaurante.pedido.cocina";
    public static final String QUEUE_BAR           = "restaurante.pedido.bar";
    public static final String QUEUE_ESTADO_PEDIDO = "restaurante.pedido.estado";

    // Cola nueva — estado individual por plato/bebida
    public static final String QUEUE_PLATO_ESTADO  = "restaurante.plato.estado";

    // Routing keys existentes
    public static final String RK_COCINA             = "pedido.cocina";
    public static final String RK_BAR                = "pedido.bar";
    public static final String RK_ESTADO_ACTUALIZADO = "pedido.estado.actualizado";

    // Routing key nueva — notificaciones por plato/bebida
    public static final String RK_PLATO_ESTADO       = "pedido.plato.estado";

    // ── Exchange ──────────────────────────────────────────────────────────────
    @Bean
    public TopicExchange gastroSenaExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // ── Queues ────────────────────────────────────────────────────────────────
    @Bean
    public Queue queueCocina() {
        return QueueBuilder.durable(QUEUE_COCINA).build();
    }

    @Bean
    public Queue queueBar() {
        return QueueBuilder.durable(QUEUE_BAR).build();
    }

    @Bean
    public Queue queueEstadoPedido() {
        return QueueBuilder.durable(QUEUE_ESTADO_PEDIDO).build();
    }

    @Bean
    public Queue queuePlatoEstado() {
        return QueueBuilder.durable(QUEUE_PLATO_ESTADO).build();
    }

    // ── Bindings ──────────────────────────────────────────────────────────────
    @Bean
    public Binding bindingCocina() {
        return BindingBuilder.bind(queueCocina())
                .to(gastroSenaExchange())
                .with(RK_COCINA);
    }

    @Bean
    public Binding bindingBar() {
        return BindingBuilder.bind(queueBar())
                .to(gastroSenaExchange())
                .with(RK_BAR);
    }

    @Bean
    public Binding bindingEstadoPedido() {
        return BindingBuilder.bind(queueEstadoPedido())
                .to(gastroSenaExchange())
                .with(RK_ESTADO_ACTUALIZADO);
    }

    @Bean
    public Binding bindingPlatoEstado() {
        return BindingBuilder.bind(queuePlatoEstado())
                .to(gastroSenaExchange())
                .with(RK_PLATO_ESTADO);
    }

    // ── Serialización JSON ────────────────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        // Usamos el ObjectMapper de Spring Boot que ya tiene:
        // - JavaTimeModule registrado
        // - write-dates-as-timestamps=false aplicado
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        // Spring inyecta el bean MessageConverter directamente —
        // ya no llamamos al método manualmente
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
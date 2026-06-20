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

    public static final String QUEUE_COCINA        = "restaurante.pedido.cocina";
    public static final String QUEUE_BAR           = "restaurante.pedido.bar";
    public static final String QUEUE_ESTADO_PEDIDO = "restaurante.pedido.estado";
    public static final String QUEUE_PLATO_ESTADO  = "restaurante.plato.estado";

    // Colas de cancelación/devolución separadas por módulo
    public static final String QUEUE_CANCELACION_COCINA = "restaurante.pedido.cancelacion.cocina";
    public static final String QUEUE_CANCELACION_BAR    = "restaurante.pedido.cancelacion.bar";

    public static final String RK_COCINA             = "pedido.cocina";
    public static final String RK_BAR                = "pedido.bar";
    public static final String RK_ESTADO_ACTUALIZADO = "pedido.estado.actualizado";
    public static final String RK_PLATO_ESTADO       = "pedido.plato.estado";

    // Routing keys de cancelación/devolución separadas por módulo
    public static final String RK_CANCELACION_COCINA = "pedido.cancelacion.cocina";
    public static final String RK_CANCELACION_BAR    = "pedido.cancelacion.bar";

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

    @Bean
    public Queue queueCancelacionCocina() {
        return QueueBuilder.durable(QUEUE_CANCELACION_COCINA).build();
    }

    @Bean
    public Queue queueCancelacionBar() {
        return QueueBuilder.durable(QUEUE_CANCELACION_BAR).build();
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

    @Bean
    public Binding bindingCancelacionCocina() {
        return BindingBuilder.bind(queueCancelacionCocina())
                .to(gastroSenaExchange())
                .with(RK_CANCELACION_COCINA);
    }

    @Bean
    public Binding bindingCancelacionBar() {
        return BindingBuilder.bind(queueCancelacionBar())
                .to(gastroSenaExchange())
                .with(RK_CANCELACION_BAR);
    }

    // ── Serialización JSON ────────────────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
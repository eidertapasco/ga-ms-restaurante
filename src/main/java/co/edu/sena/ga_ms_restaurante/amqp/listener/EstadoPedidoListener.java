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
        log.info("Evento de estado recibido de {} — pedido: {} → '{}'",
                evento.getModulo(), evento.getIdPedido(), evento.getNuevoEstado());

        try {
            EstadoPedido nuevoEstado = traducirEstado(evento.getNuevoEstado());
            pedidoService.actualizarEstadoDesdeEvento(evento.getIdPedido(), nuevoEstado);

        } catch (IllegalArgumentException e) {
            log.error("Estado desconocido recibido de {}: '{}' — evento ignorado",
                    evento.getModulo(), evento.getNuevoEstado());
        } catch (Exception e) {
            log.error("Error procesando evento de estado para pedido {}: {}",
                    evento.getIdPedido(), e.getMessage());
            // No relanzar — evita reencolas infinitas en RabbitMQ
        }
    }

    /**
     * Normaliza los estados que cualquier módulo externo (Cocina o Bar) puede enviar
     * y los traduce al enum EstadoPedido de Restaurante.
     *
     * Blindaje defensivo: aplica para ambos módulos por igual.
     * Si alguno ya envía el nombre correcto, el case "default" lo maneja sin problema.
     *
     * Traducciones conocidas:
     *   "LISTO"      → LISTO_PARA_SERVIR  (Bar usa su terminología interna)
     *   "PREPARANDO" → EN_PREPARACION     (alias que puede usar cualquier módulo)
     */
    private EstadoPedido traducirEstado(String estadoRaw) {
        if (estadoRaw == null) throw new IllegalArgumentException("Estado nulo recibido");

        String estadoNormalizado = switch (estadoRaw.toUpperCase()) {
            case "LISTO"      -> "LISTO_PARA_SERVIR";
            case "PREPARANDO" -> "EN_PREPARACION";
            default           -> estadoRaw.toUpperCase();
        };

        return EstadoPedido.valueOf(estadoNormalizado);
    }
}
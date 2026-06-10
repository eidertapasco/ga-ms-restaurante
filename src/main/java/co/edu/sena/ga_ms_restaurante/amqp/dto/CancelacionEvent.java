package co.edu.sena.ga_ms_restaurante.amqp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Evento que Restaurante publica hacia Cocina y Bar cuando se cancela
 * o devuelve un pedido (globalmente) o un ítem específico.
 *
 * Exchange : gastrosena.pedidos
 * Routing Key: pedido.cancelacion
 *
 * idPlatoEspecifico = null  → cancela/devuelve TODO el pedido
 * idPlatoEspecifico = UUID  → cancela/devuelve ese DetallePedido específico
 *
 * devolucion = false → cancelar (el plato aún no estaba listo)
 * devolucion = true  → devolver (el plato ya estaba listo / fue preparado)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelacionEvent {

    private UUID idComanda;           // = idPedido de Restaurante

    private UUID idPlatoEspecifico;   // null = global | UUID = ítem puntual

    private boolean devolucion;       // false = cancelar | true = devolver

    private String motivo;            // descripción textual del motivo
}
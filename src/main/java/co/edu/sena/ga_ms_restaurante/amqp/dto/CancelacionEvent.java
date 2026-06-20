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
 * Routing Key: pedido.cancelacion.cocina | pedido.cancelacion.bar
 *
 * idPlatoEspecifico = null  → cancela/devuelve TODO el pedido
 * idPlatoEspecifico = UUID  → cancela/devuelve ese DetallePedido específico
 *
 * devolucion = false → cancelar (el plato aún no estaba listo)
 * devolucion = true  → devolver (el plato ya estaba listo / fue preparado)
 *
 * cantidad = null  → todas las unidades activas de ese detalle (compatibilidad)
 * cantidad = N     → cancela/devuelve N unidades (parcial)
 *
 * IMPORTANTE: el nombre del campo debe ser EXACTAMENTE "cantidad";
 * Cocina y Bar deserializan por nombre de campo.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelacionEvent {

    private UUID idComanda;           // = idPedido de Restaurante

    private UUID idPlatoEspecifico;   // null = global | UUID = ítem puntual

    private boolean devolucion;       // false = cancelar | true = devolver

    private String motivo;            // descripción textual del motivo

    private Integer cantidad;         // null = todas las unidades | N = parcial
}
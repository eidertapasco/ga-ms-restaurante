package co.edu.sena.ga_ms_restaurante.pedido.service;

import co.edu.sena.ga_ms_restaurante.pedido.dto.request.PedidoCreateRequest;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResumenResponse;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResponse;
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;

import java.util.List;
import java.util.UUID;

public interface PedidoService {

    PedidoResponse crear(PedidoCreateRequest request);

    PedidoResponse confirmarYEnviar(UUID pedidoId);

    PedidoResponse marcarEntregado(UUID pedidoId);

    /** Cancela todo el pedido. Notifica a Cocina/Bar si ya había sido enviado. */
    PedidoResponse cancelar(UUID pedidoId, String motivo);

    /** Devuelve todo el pedido (ya fue preparado). Notifica a Cocina/Bar. */
    PedidoResponse devolverGlobal(UUID pedidoId, String motivo);

    /**
     * Cancela un ítem puntual (aún no estaba listo). Notifica a Cocina/Bar.
     * @param cantidad unidades a cancelar; null = todas las unidades activas.
     */
    PedidoResponse cancelarDetalle(UUID detalleId, String motivo, Integer cantidad);

    /**
     * Devuelve un ítem puntual (ya estaba listo). Notifica a Cocina/Bar.
     * @param cantidad unidades a devolver; null = todas las unidades activas.
     */
    PedidoResponse devolverDetalle(UUID detalleId, String motivo, Integer cantidad);

    PedidoResponse buscarPorId(UUID id);

    List<PedidoResumenResponse> listarPorMesero(UUID meseroId);

    List<PedidoResumenResponse> listarTodos();

    List<PedidoResumenResponse> listarPorEstado(EstadoPedido estado);

    List<PedidoResumenResponse> listarPorMesa(UUID mesaId);

    /** Llamado por EstadoPedidoListener cuando Cocina/Bar actualizan el estado. */
    void actualizarEstadoDesdeEvento(UUID pedidoId, EstadoPedido nuevoEstado);
}
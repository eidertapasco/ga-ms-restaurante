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

    PedidoResponse cancelar(UUID pedidoId);

    PedidoResponse buscarPorId(UUID id);

    List<PedidoResumenResponse> listarPorMesero(UUID meseroId);

    List<PedidoResumenResponse> listarTodos();

    List<PedidoResumenResponse> listarPorEstado(EstadoPedido estado);

    List<PedidoResumenResponse> listarPorMesa(UUID mesaId);

    // Llamado por EstadoPedidoListener cuando Cocina/Bar actualizan el estado
    void actualizarEstadoDesdeEvento(UUID pedidoId, EstadoPedido nuevoEstado);
}
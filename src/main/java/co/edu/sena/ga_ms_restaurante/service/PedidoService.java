package co.edu.sena.ga_ms_restaurante.service;

import co.edu.sena.ga_ms_restaurante.model.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.model.Pedido;

import java.util.List;

public interface PedidoService {

    Pedido crearPedido(Pedido pedido);

    Pedido obtenerPedidoPorId(Long idPedido);

    List<Pedido> listarPedidos();

    Pedido cambiarEstado(Long idPedido, EstadoPedido nuevoEstado);

    Pedido cancelarPedido(Long idPedido);
}

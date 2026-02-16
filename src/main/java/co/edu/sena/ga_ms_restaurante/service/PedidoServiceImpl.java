package co.edu.sena.ga_ms_restaurante.service;

import co.edu.sena.ga_lib_common.enums.global.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.model.*;
import co.edu.sena.ga_ms_restaurante.repository.PedidoRepository;
import co.edu.sena.ga_ms_restaurante.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;


@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                             ProductoRepository productoRepository){
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public Pedido crearPedido(Pedido pedido){

        if(pedido.getDetalles() == null || pedido.getDetalles().isEmpty()){
            throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        }

        BigDecimal total = BigDecimal.ZERO;

        for(DetallePedido detalle : pedido.getDetalles()){

            Producto producto = productoRepository.findById(detalle.getProducto().getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no existe: " + detalle.getProducto().getIdProducto()
                    ));

            if(detalle.getCantidad() <= 0){
                throw new IllegalArgumentException("Cantidad inválida");
            }

            detalle.setProducto(producto);
            detalle.setPrecioCongelado(producto.getPrecioActual());

            BigDecimal subtotal = producto.getPrecioActual()
                    .multiply(BigDecimal.valueOf(detalle.getCantidad()));

            detalle.setSubtotal(subtotal);
            total = total.add(subtotal);

            detalle.setPedido(pedido);
        }

        pedido.setTotalFinal(total);
        pedido.setEstado(EstadoPedido.ABIERTO);

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido obtenerPedidoPorId(Long idPedido){
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos(){
        return pedidoRepository.findAll();
    }

    @Override
    public Pedido cambiarEstado(Long idPedido, EstadoPedido nuevoEstado ){

        Pedido pedido = obtenerPedidoPorId(idPedido);

        if(pedido.getEstado() == EstadoPedido.ENTREGADO ||
                pedido.getEstado() == EstadoPedido.CANCELADO){
            throw new IllegalArgumentException("No se puede modificar un pedido finalizado");
        }

        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    @Override
    public Pedido cancelarPedido(Long idPedido){

        Pedido pedido = obtenerPedidoPorId(idPedido);

        if(pedido.getEstado() == EstadoPedido.ENTREGADO){
            throw new IllegalArgumentException("No se puede cancelar un pedido entregado");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        return pedidoRepository.save(pedido);
    }
}


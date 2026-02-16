package co.edu.sena.ga_ms_restaurante.mapper;

import co.edu.sena.ga_lib_common.enums.global.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.dto.request.CrearDetallePedidoRequestDTO;
import co.edu.sena.ga_ms_restaurante.dto.request.CrearPedidoRequestDTO;
import co.edu.sena.ga_ms_restaurante.dto.response.DetallePedidoResponseDTO;
import co.edu.sena.ga_ms_restaurante.dto.response.PedidoResponseDTO;
import co.edu.sena.ga_ms_restaurante.model.DetallePedido;
import co.edu.sena.ga_ms_restaurante.model.Pedido;
import co.edu.sena.ga_ms_restaurante.model.Producto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PedidoMapper {

    /* =========================
       REQUEST → ENTITY
       ========================= */

    public Pedido toEntity(CrearPedidoRequestDTO dto) {

        Pedido pedido = new Pedido();
        pedido.setIdMesa(dto.getIdMesa());
        pedido.setIdMesero(dto.getIdMesero());

        // Los detalles se crean VACÍOS.
        // El Service se encarga de:
        // - buscar productos
        // - congelar precios
        // - calcular totales
        return pedido;
    }

    public DetallePedido toDetalleEntity(CrearDetallePedidoRequestDTO dto) {

        DetallePedido detalle = new DetallePedido();

        detalle.setCantidad(dto.getCantidad());
        detalle.setObservaciones(dto.getObservaciones());

        Producto producto = new Producto();
        producto.setIdProducto(dto.getIdProducto());

        detalle.setProducto(producto);
        return detalle;
    }

    /* =========================
       ENTITY → RESPONSE
       ========================= */

    public PedidoResponseDTO toResponse(Pedido pedido) {

        PedidoResponseDTO response = new PedidoResponseDTO();
        response.setIdPedido(pedido.getIdPedido());
        response.setIdMesa(pedido.getIdMesa());
        response.setIdMesero(pedido.getIdMesero());
        response.setTotalFinal(pedido.getTotalFinal());
        response.setEstado(pedido.getEstado().name());
        response.setCreatedAt(pedido.getCreatedAt());

        response.setDetalles(
                pedido.getDetalles().stream()
                        .map(this::toDetalleResponse)
                        .collect(Collectors.toList())
        );

        return response;
    }

    private DetallePedidoResponseDTO toDetalleResponse(DetallePedido detalle) {

        DetallePedidoResponseDTO dto = new DetallePedidoResponseDTO();
        dto.setIdDetalle(detalle.getIdDetalle());
        dto.setIdProducto(detalle.getProducto().getIdProducto());
        dto.setNombreProducto(detalle.getProducto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioCongelado());
        dto.setSubtotal(detalle.getSubtotal());

        return dto;
    }
}


package co.edu.sena.ga_ms_restaurante.pedido.mapper;

import co.edu.sena.ga_ms_restaurante.pedido.dto.request.DetallePedidoRequest;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.DetallePedidoResponse;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResumenResponse;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResponse;
import co.edu.sena.ga_ms_restaurante.pedido.model.DetallePedido;
import co.edu.sena.ga_ms_restaurante.pedido.model.Pedido;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PedidoMapper {

    public DetallePedido toDetalleEntity(DetallePedidoRequest req) {
        DetallePedido detalle = new DetallePedido();
        detalle.setProductoId(req.getProductoId());
        detalle.setNombreProducto(req.getNombreProducto());
        detalle.setCantidad(req.getCantidad());
        detalle.setPrecioUnitario(req.getPrecioUnitario());
        detalle.setSubtotalLinea(
                req.getPrecioUnitario().multiply(java.math.BigDecimal.valueOf(req.getCantidad()))
        );
        detalle.setCategoria(req.getCategoria());
        detalle.setObservaciones(req.getObservaciones());
        return detalle;
    }

    public DetallePedidoResponse toDetalleResponse(DetallePedido detalle) {
        DetallePedidoResponse resp = new DetallePedidoResponse();
        resp.setId(detalle.getId());
        resp.setProductoId(detalle.getProductoId());
        resp.setNombreProducto(detalle.getNombreProducto());
        resp.setCantidad(detalle.getCantidad());
        resp.setPrecioUnitario(detalle.getPrecioUnitario());
        resp.setSubtotalLinea(detalle.getSubtotalLinea());
        //resp.setCategoria(detalle.getCategoria());
        resp.setObservaciones(detalle.getObservaciones());
        resp.setEstadoDetalle(detalle.getEstadoDetalle());
        return resp;
    }

    public PedidoResponse toResponse(Pedido pedido) {
        PedidoResponse resp = new PedidoResponse();
        resp.setId(pedido.getId());
        resp.setMesaId(pedido.getMesa().getId());
        resp.setNombreMesa(pedido.getMesa().getNombre());
        resp.setMeseroId(pedido.getMeseroId());
        resp.setNumeroComensales(pedido.getNumeroComensales());
        resp.setNotas(pedido.getNotas());
        resp.setEstado(pedido.getEstado());
        resp.setSubtotal(pedido.getSubtotal());
        resp.setFechaCreacion(pedido.getFechaCreacion());
        resp.setFechaCierre(pedido.getFechaCierre());
        resp.setDetalles(
                pedido.getDetalles().stream().map(this::toDetalleResponse).toList()
        );
        return resp;
    }

    public PedidoResumenResponse toResumen(Pedido pedido) {
        PedidoResumenResponse resp = new PedidoResumenResponse();
        resp.setId(pedido.getId());
        resp.setNombreMesa(pedido.getMesa().getNombre());
        resp.setMeseroId(pedido.getMeseroId());
        resp.setNumeroComensales(pedido.getNumeroComensales());
        resp.setEstado(pedido.getEstado());
        resp.setSubtotal(pedido.getSubtotal());
        resp.setFechaCreacion(pedido.getFechaCreacion());
        return resp;
    }

    public List<PedidoResumenResponse> toResumenList(List<Pedido> pedidos) {
        return pedidos.stream().map(this::toResumen).toList();
    }
}
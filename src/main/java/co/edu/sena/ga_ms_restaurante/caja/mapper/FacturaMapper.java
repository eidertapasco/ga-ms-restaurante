package co.edu.sena.ga_ms_restaurante.caja.mapper;

import co.edu.sena.ga_ms_restaurante.caja.dto.response.FacturaResponse;
import co.edu.sena.ga_ms_restaurante.caja.model.Factura;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FacturaMapper {

    public FacturaResponse toFacturaResponse(Factura factura) {
        FacturaResponse resp = new FacturaResponse();
        resp.setId(factura.getId());
        resp.setNumeroFactura(factura.getNumeroFactura());
        resp.setPedidoId(factura.getPedido().getId());
        resp.setNombreMesa(factura.getPedido().getMesa().getNombre());
        resp.setSesionCajaId(factura.getSesionCaja().getId());
        resp.setCajeroId(factura.getCajeroId());
        resp.setSubtotal(factura.getSubtotal());
        resp.setPropina(factura.getPropina());
        resp.setTotal(factura.getTotal());
        resp.setMetodoPago(factura.getMetodoPago());
        resp.setEstado(factura.getEstado());
        resp.setFechaEmision(factura.getFechaEmision());
        return resp;
    }

    public List<FacturaResponse> toFacturaResponseList(List<Factura> facturas) {
        return facturas.stream().map(this::toFacturaResponse).toList();
    }
}
package co.edu.sena.ga_ms_restaurante.caja.mapper;

import co.edu.sena.ga_ms_restaurante.caja.dto.response.FacturaResponse;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.SesionCajaResponse;
import co.edu.sena.ga_ms_restaurante.caja.model.Factura;
import co.edu.sena.ga_ms_restaurante.caja.model.SesionCaja;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CajaMapper {

    public SesionCajaResponse toSesionResponse(SesionCaja sesion) {
        SesionCajaResponse resp = new SesionCajaResponse();
        resp.setId(sesion.getId());
        resp.setCajeroId(sesion.getCajeroId());
        resp.setEstado(sesion.getEstado());
        resp.setBaseEfectivo(sesion.getBaseEfectivo());
        resp.setTotalVentasEfectivo(orZero(sesion.getTotalVentasEfectivo()));
        resp.setTotalVentasTarjeta(orZero(sesion.getTotalVentasTarjeta()));
        resp.setTotalVentasTransferencia(orZero(sesion.getTotalVentasTransferencia()));
        resp.setEfectivoReal(sesion.getEfectivoReal());
        resp.setDiferencia(sesion.getDiferencia());
        resp.setFechaApertura(sesion.getFechaApertura());
        resp.setFechaCierre(sesion.getFechaCierre());
        return resp;
    }

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

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
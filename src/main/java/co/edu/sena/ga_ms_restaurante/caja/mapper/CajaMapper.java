package co.edu.sena.ga_ms_restaurante.caja.mapper;

import co.edu.sena.ga_ms_restaurante.caja.dto.response.SesionCajaResponse;
import co.edu.sena.ga_ms_restaurante.caja.model.SesionCaja;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
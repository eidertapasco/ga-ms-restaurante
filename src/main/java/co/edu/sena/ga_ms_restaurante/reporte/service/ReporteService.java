package co.edu.sena.ga_ms_restaurante.reporte.service;

import co.edu.sena.ga_ms_restaurante.reporte.dto.response.PedidoPorMesaResponse;
import co.edu.sena.ga_ms_restaurante.reporte.dto.response.ResumenVentasResponse;

import java.util.List;
import java.util.UUID;

public interface ReporteService {

    ResumenVentasResponse resumenVentasPorSesion(UUID sesionId);

    List<PedidoPorMesaResponse> pedidosPorMesa();
}

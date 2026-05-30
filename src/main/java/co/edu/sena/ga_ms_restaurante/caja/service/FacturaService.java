package co.edu.sena.ga_ms_restaurante.caja.service;

import co.edu.sena.ga_ms_restaurante.caja.dto.request.FacturarPedidoRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.FacturaResponse;

import java.util.List;
import java.util.UUID;

public interface FacturaService {
    FacturaResponse facturar(FacturarPedidoRequest request);
    FacturaResponse anularFactura(UUID facturaId);
    FacturaResponse buscarFacturaPorId(UUID facturaId);
    FacturaResponse buscarFacturaPorNumero(String numeroFactura);
    List<FacturaResponse> listarFacturasDeSesion(UUID sesionId);
}
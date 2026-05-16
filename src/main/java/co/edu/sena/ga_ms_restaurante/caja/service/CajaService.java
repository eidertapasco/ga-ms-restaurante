package co.edu.sena.ga_ms_restaurante.caja.service;

import co.edu.sena.ga_ms_restaurante.caja.dto.request.AbrirSesionRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.request.CerrarSesionRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.request.FacturarPedidoRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.FacturaResponse;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.SesionCajaResponse;

import java.util.List;
import java.util.UUID;

public interface CajaService {

    SesionCajaResponse abrirSesion(AbrirSesionRequest request);

    SesionCajaResponse cerrarSesion(UUID sesionId, CerrarSesionRequest request);

    SesionCajaResponse buscarSesionPorId(UUID sesionId);

    SesionCajaResponse sesionActiva();

    FacturaResponse facturar(FacturarPedidoRequest request);

    FacturaResponse anularFactura(UUID facturaId);

    FacturaResponse buscarFacturaPorId(UUID facturaId);

    FacturaResponse buscarFacturaPorNumero(String numeroFactura);

    List<FacturaResponse> listarFacturasDeSesion(UUID sesionId);
}

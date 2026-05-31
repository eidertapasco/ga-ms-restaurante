package co.edu.sena.ga_ms_restaurante.caja.service;

import co.edu.sena.ga_ms_restaurante.caja.dto.request.AbrirSesionRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.request.CerrarSesionRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.SesionCajaResponse;

import java.util.UUID;

public interface CajaService {
    SesionCajaResponse abrirSesion(AbrirSesionRequest request);
    SesionCajaResponse cerrarSesion(UUID sesionId, CerrarSesionRequest request);
    SesionCajaResponse buscarSesionPorId(UUID sesionId);
    SesionCajaResponse sesionActiva();
}
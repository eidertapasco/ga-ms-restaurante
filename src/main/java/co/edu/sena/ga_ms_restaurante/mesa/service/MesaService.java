package co.edu.sena.ga_ms_restaurante.mesa.service;

import co.edu.sena.ga_ms_restaurante.mesa.dto.request.MesaCreateRequest;
import co.edu.sena.ga_ms_restaurante.mesa.dto.request.MesaUpdateRequest;
import co.edu.sena.ga_ms_restaurante.mesa.dto.response.MesaResponse;
import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;

import java.util.List;
import java.util.UUID;

public interface MesaService {

    List<MesaResponse> listarMesasActivas();

    List<MesaResponse> listarMesasPorEstado(EstadoMesa estado);

    MesaResponse buscarPorId(UUID id);

    MesaResponse crear(MesaCreateRequest request);

    MesaResponse actualizar(UUID id, MesaUpdateRequest request);

    MesaResponse cambiarEstado(UUID id, EstadoMesa nuevoEstado);

    MesaResponse activar(UUID id);

    MesaResponse desactivar(UUID id);
}

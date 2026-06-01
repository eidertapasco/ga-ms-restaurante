package co.edu.sena.ga_ms_restaurante.mesa.mapper;

import co.edu.sena.ga_ms_restaurante.mesa.dto.request.MesaCreateRequest;
import co.edu.sena.ga_ms_restaurante.mesa.dto.request.MesaUpdateRequest;
import co.edu.sena.ga_ms_restaurante.mesa.dto.response.MesaResponse;
import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.mesa.model.Mesa;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MesaMapper {

    public MesaResponse toResponse(Mesa mesa) {
        return MesaResponse.builder()
                .id(mesa.getId())
                .nombre(mesa.getNombre())
                .capacidad(mesa.getCapacidad())
                .zona(mesa.getZona())
                .observaciones(mesa.getObservaciones())
                .estado(mesa.getEstado())
                .activo(mesa.getActivo())
                .build();
    }

    public List<MesaResponse> toResponseList(List<Mesa> mesas) {
        return mesas.stream()
                .map(this::toResponse)
                .toList();
    }

    public Mesa toEntity(MesaCreateRequest request) {
        return Mesa.builder()
                .nombre(request.getNombre())
                .capacidad(request.getCapacidad())
                .zona(request.getZona())
                .observaciones(request.getObservaciones()) // AÑADIDO
                .estado(EstadoMesa.LIBRE)
                .activo(true)
                .build();
    }

    public void updateEntity(Mesa mesa, MesaUpdateRequest request) {
        if (request.getNombre() != null) mesa.setNombre(request.getNombre());
        if (request.getCapacidad() != null) mesa.setCapacidad(request.getCapacidad());
        if (request.getZona() != null) mesa.setZona(request.getZona());
        if (request.getObservaciones() != null) mesa.setObservaciones(request.getObservaciones()); // AÑADIDO
    }
}
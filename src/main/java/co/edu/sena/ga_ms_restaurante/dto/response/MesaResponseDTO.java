package co.edu.sena.ga_ms_restaurante.dto.response;

import java.util.UUID;

public record MesaResponseDTO(
        UUID idMesa,
        String numeroMesa,
        Integer capacidad,
        String estado) {
}

package co.edu.sena.ga_ms_restaurante.dto.response;

import co.edu.sena.ga_ms_restaurante.enums.EstadoMesa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesaResponseDTO {
    private UUID id;
    private Integer numero;
    private String capacidad;
    private EstadoMesa estado;
}

package co.edu.sena.ga_ms_restaurante.mesa.dto.response;

import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MesaResponse {

    private UUID id;
    private String nombre;
    private Integer capacidad;
    private String zona;
    private EstadoMesa estado;
    private Boolean activo;
}

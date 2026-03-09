package co.edu.sena.ga_ms_restaurante.dto.request;

import co.edu.sena.ga_ms_restaurante.enums.EstadoMesa;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearMesaRequestDTO {
    @NotNull
    private Integer numero;
    @NotNull
    private String capacidad;
    @NotNull
    private EstadoMesa estado;
}

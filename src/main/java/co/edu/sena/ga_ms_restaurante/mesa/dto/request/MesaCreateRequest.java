package co.edu.sena.ga_ms_restaurante.mesa.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MesaCreateRequest {

    @NotBlank(message = "El nombre de la mesa es obligatorio")
    @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
    private String nombre;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad mínima es 1")
    @Max(value = 20, message = "La capacidad máxima es 20")
    private Integer capacidad;

    @Size(max = 50, message = "La zona no puede superar 50 caracteres")
    private String zona;
}

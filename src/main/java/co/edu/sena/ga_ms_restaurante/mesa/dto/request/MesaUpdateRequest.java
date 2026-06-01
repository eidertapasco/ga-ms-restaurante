package co.edu.sena.ga_ms_restaurante.mesa.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MesaUpdateRequest {

    @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
    private String nombre;

    @Min(value = 1, message = "La capacidad mínima es 1")
    @Max(value = 20, message = "La capacidad máxima es 20")
    private Integer capacidad;

    @Size(max = 50, message = "La zona no puede superar 50 caracteres")
    private String zona;

    @Size(max = 255, message = "Las observaciones no pueden superar 255 caracteres")
    private String observaciones;
}
package co.edu.sena.ga_ms_restaurante.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesaRequestDTO {
    @NotNull(message = "El número de mesa es obligatorio")
    @Min(value = 1, message = "El número de mesa debe ser mayor a 0")
    private Integer numeroMesa;

    @NotBlank(message = "La capacidad es obligatoria")
    private String capacidad;
    // Se elimina 'estado', lo maneja el Service internamente.
}
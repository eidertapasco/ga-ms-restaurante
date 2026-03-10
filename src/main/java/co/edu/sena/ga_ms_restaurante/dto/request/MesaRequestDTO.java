package co.edu.sena.ga_ms_restaurante.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesaRequestDTO {

    @NotBlank(message = "El identificador de la mesa es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "El número de mesa solo acepta letras, números y guiones (ej. M-1)")
    private String numeroMesa;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    private Integer capacidad;

    // Se elimina 'estado', lo maneja el Service internamente, la mesa nace 'DISPONIBLE'.
}
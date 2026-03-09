package co.edu.sena.ga_ms_restaurante.dto.request;
import jakarta.validation.constraints.NotBlank;

public record CrearMesaRequestDTO(
        @NotBlank(message = "El número de la mesa no puede estar vacío")
        String numeroMesa,
        @NotBlank(message = "La capacidad no puede estar vacía")
        Integer capacidad) {
}

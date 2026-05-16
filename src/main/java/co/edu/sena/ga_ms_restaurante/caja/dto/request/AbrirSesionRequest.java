package co.edu.sena.ga_ms_restaurante.caja.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AbrirSesionRequest {

    @NotNull(message = "La base de efectivo es obligatoria")
    @DecimalMin(value = "0.0", inclusive = true, message = "La base no puede ser negativa")
    private BigDecimal baseEfectivo;
}
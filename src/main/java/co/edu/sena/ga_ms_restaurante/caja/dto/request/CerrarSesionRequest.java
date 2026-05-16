package co.edu.sena.ga_ms_restaurante.caja.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CerrarSesionRequest {

    @NotNull(message = "El efectivo real contado es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El efectivo real no puede ser negativo")
    private BigDecimal efectivoReal;
}

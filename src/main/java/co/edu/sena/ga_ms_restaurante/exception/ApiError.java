package co.edu.sena.ga_ms_restaurante.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private int codigo;
    private String mensaje;
}

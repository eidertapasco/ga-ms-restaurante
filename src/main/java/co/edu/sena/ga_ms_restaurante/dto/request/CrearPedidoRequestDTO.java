package co.edu.sena.ga_ms_restaurante.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearPedidoRequestDTO {

    @NotNull
    private Long idMesa;

    @NotNull
    private Long idMesero;

    @NotNull
    @Size(min = 1)
    private List<CrearDetallePedidoRequestDTO> detalles;

}

package co.edu.sena.ga_ms_restaurante.amqp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoCocinaEvent {

    private UUID idPedido;
    private String numeroMesa;
    private String notas;
    private List<Item> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private String idDetallePedido;  // NUEVO — UUID del DetallePedido en texto
        private String idProducto;
        private String nombreProducto;
        private int cantidad;
        private String observaciones;
    }
}
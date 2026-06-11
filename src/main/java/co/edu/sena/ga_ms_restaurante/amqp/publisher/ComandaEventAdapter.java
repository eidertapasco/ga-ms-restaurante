package co.edu.sena.ga_ms_restaurante.amqp.publisher;

import co.edu.sena.ga_ms_restaurante.amqp.dto.ComandaRequestDTO;
import co.edu.sena.ga_ms_restaurante.amqp.dto.PedidoBarEvent;
import co.edu.sena.ga_ms_restaurante.amqp.dto.PedidoCocinaEvent;
import co.edu.sena.ga_ms_restaurante.pedido.model.Pedido;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Traduce los eventos internos de Restaurante al formato ComandaRequestDTO
 * que esperan Cocina y Bar/Barismo. Aquí vive toda la lógica de adaptación
 * de nomenclatura; el resto del código no sabe nada de los campos externos.
 *
 * Regla 1: los nombres internos de Restaurante no se tocan. La traducción
 *          (items→detalles, notas→notasAdicionales, etc.) ocurre solo aquí.
 * Regla 2: campo 'prioridad' no se incluye.
 * Regla 3: 'nombreMesero' se mockea hasta que el módulo de Usuarios esté listo.
 */
@Component
public class ComandaEventAdapter {

    /**
     * Para la cola de Cocina.
     * Los ítems ya deben estar filtrados (solo COMIDA).
     */
    public ComandaRequestDTO traducirParaCocina(Pedido pedido,
                                                List<PedidoCocinaEvent.Item> items) {
        return buildDto(pedido, items.stream()
                .map(item -> ComandaRequestDTO.DetalleComandaDto.builder()
                        .idDetallePedido(item.getIdDetallePedido())
                        .idReceta(item.getIdProducto())
                        .cantidad(item.getCantidad())
                        .observacionesPlato(item.getObservaciones())
                        .build())
                .toList());
    }

    /**
     * Para la cola de Bar/Barismo.
     * Los ítems ya deben estar filtrados (solo BEBIDA).
     */
    public ComandaRequestDTO traducirParaBar(Pedido pedido,
                                             List<PedidoBarEvent.Item> items) {
        return buildDto(pedido, items.stream()
                .map(item -> ComandaRequestDTO.DetalleComandaDto.builder()
                        .idDetallePedido(item.getIdDetallePedido())
                        .idReceta(item.getIdProducto())
                        .cantidad(item.getCantidad())
                        .observacionesPlato(item.getObservaciones())
                        .build())
                .toList());
    }

    // ─── helpers privados ────────────────────────────────────────────────────

    private ComandaRequestDTO buildDto(Pedido pedido,
                                       List<ComandaRequestDTO.DetalleComandaDto> detalles) {
        return ComandaRequestDTO.builder()
                .idPedidoRestaurante(pedido.getId())
                .idMesero(pedido.getMeseroId())
                .nombreMesero(mockNombreMesero(pedido))
                .idMesa(pedido.getMesa().getId())
                .numeroMesa(extraerNumeroMesa(pedido.getMesa().getNombre()))
                .fechaPedido(LocalDateTime.now())
                .notasAdicionales(pedido.getNotas())
                // prioridad omitida — Regla 2
                .detalles(detalles)
                .build();
    }

    /**
     * Mock temporal. Cuando el módulo de Usuarios esté listo,
     * se reemplaza esta línea por una llamada a su API.
     */
    private String mockNombreMesero(Pedido pedido) {
        return "Mesero " + pedido.getMeseroId().toString().substring(0, 8);
    }

    /**
     * "Mesa 03" → 3 | "Barra 01" → 1 | texto sin dígitos → null
     */
    private Integer extraerNumeroMesa(String nombreMesa) {
        if (nombreMesa == null || nombreMesa.isBlank()) return null;
        String soloDigitos = nombreMesa.replaceAll("[^0-9]", "");
        if (soloDigitos.isEmpty()) return null;
        try {
            return Integer.parseInt(soloDigitos);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
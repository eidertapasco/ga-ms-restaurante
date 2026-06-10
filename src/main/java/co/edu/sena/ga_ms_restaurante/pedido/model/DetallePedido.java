package co.edu.sena.ga_ms_restaurante.pedido.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "detalles_pedido")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private UUID id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "producto_id", nullable = false)
    private String productoId; // String intencional — Cocina define el tipo de su ID

    @Column(name = "nombre_producto", nullable = false)
    private String nombreProducto; // congelado al momento del pedido

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario; // congelado al momento del pedido

    @Column(name = "subtotal_linea", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalLinea; // cantidad * precioUnitario

    @Column(nullable = false)
    private String categoria; // "COMIDA" | "BEBIDA"

    @Column(length = 255)
    private String observaciones;

    /**
     * Estado informativo del ítem según Cocina o Bar.
     * No afecta la lógica de negocio (esa la gobierna Pedido.estado).
     * Se actualiza vía EstadoPlatoListener cuando Cocina/Bar notifican
     * que un plato o bebida individual cambió de estado.
     *
     * Valores: "PENDIENTE" | "PREPARANDO" | "TERMINADO" | "LISTO"
     * La columna se crea automáticamente con ddl-auto=update.
     */
    @Column(name = "estado_detalle", length = 30)
    private String estadoDetalle = "PENDIENTE";
}
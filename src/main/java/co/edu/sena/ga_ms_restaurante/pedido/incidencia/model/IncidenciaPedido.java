package co.edu.sena.ga_ms_restaurante.pedido.incidencia.model;

import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.pedido.incidencia.enums.EstadoIncidencia;
import co.edu.sena.ga_ms_restaurante.pedido.incidencia.enums.TipoIncidencia;
import co.edu.sena.ga_ms_restaurante.pedido.model.DetallePedido;
import co.edu.sena.ga_ms_restaurante.pedido.model.Pedido;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Historial propio de Restaurante de cancelaciones y devoluciones.
 */
@Entity
@Table(name = "incidencias_pedido")
@Data
public class IncidenciaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    /** Null = incidencia GLOBAL del pedido completo. No null = afecta un ítem puntual. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_pedido_id")
    private DetallePedido detalle;

    /**
     * Copia del nombre del producto al momento de registrar la incidencia.
     * Mismo criterio que DetallePedido.nombreProducto: se "congela" para que
     * el historial no cambie si el producto se renombra después. Null si es global.
     */
    @Column(name = "nombre_producto_snapshot")
    private String nombreProductoSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoIncidencia tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoIncidencia estado;

    @Column(name = "cantidad_afectada")
    private Integer cantidadAfectada;

    @Column(length = 500)
    private String motivo;

    @Column(name = "registrada_por", columnDefinition = "VARCHAR(36)")
    private UUID registradaPor;

    /**
     * Estado en que estaba el Pedido justo antes de esta incidencia.
     * Permite saber a qué estado regresar cuando Cocina/Bar confirmen que el
     * reproceso terminó (p. ej. si ya estaba ENTREGADO, no debe "retroceder" a
     * LISTO_PARA_SERVIR).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pedido_previo", length = 30)
    private EstadoPedido estadoPedidoPrevio;

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;
}
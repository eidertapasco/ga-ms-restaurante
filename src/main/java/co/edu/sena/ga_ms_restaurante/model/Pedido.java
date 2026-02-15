package co.edu.sena.ga_ms_restaurante.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "id_mesa", nullable = false)
    private Long idMesa;

    @Column(name = "id_mesero", nullable = false)
    private Long idMesero;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPedido estado;

    @Column(
            name = "total_final",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalFinal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DetallePedido> detalles = new ArrayList<>();

    /* =====================
       Hooks de ciclo de vida
       ===================== */

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.estado == null) {
            this.estado = EstadoPedido.REGISTRADO;
        }
        if (this.totalFinal == null) {
            this.totalFinal = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /* =====================
       Métodos de dominio
       ===================== */

    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }

    public void limpiarDetalles() {
        detalles.clear();
    }
}


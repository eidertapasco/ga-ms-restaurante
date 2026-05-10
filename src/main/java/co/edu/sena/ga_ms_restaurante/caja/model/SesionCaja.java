package co.edu.sena.ga_ms_restaurante.caja.model;

import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoSesion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sesiones_caja")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "cajero_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID cajeroId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSesion estado;

    @Column(name = "base_efectivo", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseEfectivo;

    @Column(name = "total_ventas_efectivo", precision = 12, scale = 2)
    private BigDecimal totalVentasEfectivo;

    @Column(name = "total_ventas_tarjeta", precision = 12, scale = 2)
    private BigDecimal totalVentasTarjeta;

    @Column(name = "total_ventas_transferencia", precision = 12, scale = 2)
    private BigDecimal totalVentasTransferencia;

    @Column(name = "efectivo_real", precision = 12, scale = 2)
    private BigDecimal efectivoReal; // lo que el cajero cuenta físicamente al cerrar

    @Column(precision = 12, scale = 2)
    private BigDecimal diferencia; // efectivoReal - (baseEfectivo + totalVentasEfectivo)

    @CreationTimestamp
    @Column(name = "fecha_apertura", updatable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;
}

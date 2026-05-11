package co.edu.sena.ga_ms_restaurante.mesa.model;

import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "mesas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(length = 50)
    private String zona;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMesa estado;

    @Column(nullable = false)
    private Boolean activo;
}
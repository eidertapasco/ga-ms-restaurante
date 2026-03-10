package co.edu.sena.ga_ms_restaurante.model;

import co.edu.sena.ga_ms_restaurante.enums.EstadoMesa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "mesas")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idMesa;

    @Column( name = "numero_mesa", nullable = false, unique = true)
    private String numeroMesa;

    @Column(name = "capacidad",nullable = false, length = 50)
    private Integer capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMesa estado;
}

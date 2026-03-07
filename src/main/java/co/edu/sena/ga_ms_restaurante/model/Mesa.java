package co.edu.sena.ga_ms_restaurante.model;


import co.edu.sena.ga_ms_restaurante.enums.EstadoMesa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;


@Entity
@Table(name = "mesas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column( name = "numero", nullable = false, unique = true)
    private Integer numero;

    @Column(name = "capacidad",nullable = false, length = 50)
    private String capacidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMesa estado;
}

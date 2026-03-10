package co.edu.sena.ga_ms_restaurante.repository;

import co.edu.sena.ga_ms_restaurante.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, UUID>{

    // Necesario para que el Service valide si la mesa ya existe
    boolean existsByNumeroMesa(String numeroMesa);

    // Útil por si luego necesitan buscar una mesa específica por su número
    Optional<Mesa> findByNumeroMesa(String numeroMesa);
}

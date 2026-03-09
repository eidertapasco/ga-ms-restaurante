package co.edu.sena.ga_ms_restaurante.repository;

import co.edu.sena.ga_ms_restaurante.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, UUID>{
    boolean existsByNumeroMesa(String numeroMesa);
    Optional<Mesa> findByNumeroMesa(String numeroMesa);
}

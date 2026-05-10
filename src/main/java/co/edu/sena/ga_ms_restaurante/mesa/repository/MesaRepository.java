package co.edu.sena.ga_ms_restaurante.mesa.repository;

import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.mesa.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, UUID> {

    List<Mesa> findByActivoTrue();

    List<Mesa> findByActivoTrueAndEstado(EstadoMesa estado);

    Optional<Mesa> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}

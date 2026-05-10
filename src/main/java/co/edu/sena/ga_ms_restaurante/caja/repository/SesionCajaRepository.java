package co.edu.sena.ga_ms_restaurante.caja.repository;

import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoSesion;
import co.edu.sena.ga_ms_restaurante.caja.model.SesionCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface SesionCajaRepository extends JpaRepository<SesionCaja, UUID> {

    Optional<SesionCaja> findByEstado(EstadoSesion estado);

    boolean existsByEstado(EstadoSesion estado);

    List<SesionCaja> findByCajeroId(UUID cajeroId);
}

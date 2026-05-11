package co.edu.sena.ga_ms_restaurante.caja.repository;

import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoFactura;
import co.edu.sena.ga_ms_restaurante.caja.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, UUID> {

    Optional<Factura> findByPedido_Id(UUID pedidoId);

    Optional<Factura> findByNumeroFactura(String numeroFactura);

    List<Factura> findBySesionCaja_Id(UUID sesionCajaId);

    List<Factura> findBySesionCaja_IdAndEstado(UUID sesionCajaId, EstadoFactura estado);
}

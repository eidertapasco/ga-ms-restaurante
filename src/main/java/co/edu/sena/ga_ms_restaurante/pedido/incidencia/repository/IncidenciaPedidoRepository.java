package co.edu.sena.ga_ms_restaurante.pedido.incidencia.repository;

import co.edu.sena.ga_ms_restaurante.pedido.incidencia.enums.EstadoIncidencia;
import co.edu.sena.ga_ms_restaurante.pedido.incidencia.model.IncidenciaPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidenciaPedidoRepository extends JpaRepository<IncidenciaPedido, UUID> {

    List<IncidenciaPedido> findByPedido_IdOrderByFechaRegistroDesc(UUID pedidoId);

    Optional<IncidenciaPedido> findByDetalle_IdAndEstado(UUID detalleId, EstadoIncidencia estado);

    /** El registro que guardó a qué estado volver cuando se cierre la devolución en curso. */
    Optional<IncidenciaPedido> findFirstByPedido_IdAndEstadoPedidoPrevioIsNotNullOrderByFechaRegistroDesc(UUID pedidoId);
}
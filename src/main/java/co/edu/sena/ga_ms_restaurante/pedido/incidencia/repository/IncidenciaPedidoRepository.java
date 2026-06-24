package co.edu.sena.ga_ms_restaurante.pedido.incidencia.repository;

import co.edu.sena.ga_ms_restaurante.pedido.incidencia.enums.EstadoIncidencia;
import co.edu.sena.ga_ms_restaurante.pedido.incidencia.model.IncidenciaPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidenciaPedidoRepository extends JpaRepository<IncidenciaPedido, UUID> {

    /** Usado en este bloque para exponer el historial en GET /api/pedidos/{id}. */
    List<IncidenciaPedido> findByPedido_IdOrderByFechaRegistroDesc(UUID pedidoId);

    /**
     * Se usará a partir de la rama feature/reglas-cancelacion-devolucion para
     * encontrar la incidencia abierta de un ítem y cerrarla cuando Cocina/Bar
     * confirmen que el reproceso terminó. Se declara ya porque es solo acceso
     * a datos (sin lógica de negocio), no hace daño tenerla lista.
     */
    Optional<IncidenciaPedido> findByDetalle_IdAndEstado(UUID detalleId, EstadoIncidencia estado);
}
package co.edu.sena.ga_ms_restaurante.pedido.repository;

import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.pedido.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    List<Pedido> findByMeseroId(UUID meseroId);

    List<Pedido> findByMesa_Id(UUID mesaId);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByMeseroIdAndEstadoIn(UUID meseroId, List<EstadoPedido> estados);

    Optional<Pedido> findByMesa_IdAndEstadoIn(UUID mesaId, List<EstadoPedido> estados);
}

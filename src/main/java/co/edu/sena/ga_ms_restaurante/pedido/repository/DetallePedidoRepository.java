package co.edu.sena.ga_ms_restaurante.pedido.repository;

import co.edu.sena.ga_ms_restaurante.pedido.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, UUID> {

    List<DetallePedido> findByPedido_Id(UUID pedidoId);
}

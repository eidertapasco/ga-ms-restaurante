package co.edu.sena.ga_ms_restaurante.repository;

import co.edu.sena.ga_ms_restaurante.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Métodos personalizados se agregan SOLO si el Service lo necesita
}

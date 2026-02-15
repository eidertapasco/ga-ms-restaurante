package co.edu.sena.ga_ms_restaurante.repository;

import co.edu.sena.ga_ms_restaurante.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
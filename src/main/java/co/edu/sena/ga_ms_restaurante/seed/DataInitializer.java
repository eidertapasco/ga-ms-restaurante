package co.edu.sena.ga_ms_restaurante.seed;

import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.mesa.model.Mesa;
import co.edu.sena.ga_ms_restaurante.mesa.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MesaRepository mesaRepository;

    @Override
    public void run(String... args) {
        if (mesaRepository.count() > 0) {
            log.info("DataInitializer — datos ya existen, omitiendo seed");
            return;
        }

        List<Mesa> mesas = List.of(
                crearMesa("Mesa 01", 4, "Salón Principal"),
                crearMesa("Mesa 02", 4, "Salón Principal"),
                crearMesa("Mesa 03", 6, "Salón Principal"),
                crearMesa("Mesa 04", 6, "Salón Principal"),
                crearMesa("Mesa 05", 2, "Terraza"),
                crearMesa("Mesa 06", 2, "Terraza"),
                crearMesa("Mesa 07", 8, "Salón VIP"),
                crearMesa("Mesa 08", 8, "Salón VIP"),
                crearMesa("Barra 01", 1, "Barra"),
                crearMesa("Barra 02", 1, "Barra")
        );

        mesaRepository.saveAll(mesas);
        log.info("DataInitializer — {} mesas creadas", mesas.size());
    }

    private Mesa crearMesa(String nombre, int capacidad, String zona) {
        Mesa mesa = new Mesa();
        mesa.setNombre(nombre);
        mesa.setCapacidad(capacidad);
        mesa.setZona(zona);
        mesa.setEstado(EstadoMesa.LIBRE);
        mesa.setActivo(true);
        return mesa;
    }
}
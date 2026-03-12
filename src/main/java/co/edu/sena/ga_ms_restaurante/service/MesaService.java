package co.edu.sena.ga_ms_restaurante.service;

import co.edu.sena.ga_ms_restaurante.dto.request.MesaRequestDTO;
import co.edu.sena.ga_ms_restaurante.dto.response.MesaResponseDTO;
import co.edu.sena.ga_ms_restaurante.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.model.Mesa;
import co.edu.sena.ga_ms_restaurante.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor //crea un constructor para todos los campos marcados como final
public class MesaService {

    private final MesaRepository mesaRepository;

    //CREAR UNA MESA
    public MesaResponseDTO crearMesa(MesaRequestDTO requestDTO) {

        // 1. Validar que 'M-1' o 'Mesa-1' ya existe
        if (mesaRepository.existsByNumeroMesa(requestDTO.getNumeroMesa())) {
            throw new IllegalArgumentException("Ya existe una mesa identificada como: " + requestDTO.getNumeroMesa());
        }

        // 1.2. Construir la entidad
        Mesa nuevaMesa = Mesa.builder()
                .numeroMesa(requestDTO.getNumeroMesa().toUpperCase()) //Guardar todas en mayusculas
                .capacidad(requestDTO.getCapacidad())
                .estado(EstadoMesa.DISPONIBLE)
                .build();

        // 1.3. Persistir
        Mesa mesaGuardada = mesaRepository.save(nuevaMesa);

        // 1.4. Retornar el response DTO usando el record
        return new MesaResponseDTO(
                mesaGuardada.getIdMesa(),
                mesaGuardada.getNumeroMesa(),
                mesaGuardada.getCapacidad(),
                mesaGuardada.getEstado().name()
        );
    }

    //1. OBTENER TODAS LAS MESAS
    public List<MesaResponseDTO> listarTodas () {
        return mesaRepository.findAll() //Buscamos todas las entidades
                .stream() //convertimos a flujo de datos
                .map(this::convertirAEntityADto) //mapeamos cada una a DTO
                .collect(Collectors.toList()); //volvemos a lista
    }

    //2. BUSCAR POR ID (Para ver detalles de una mesa)
    public MesaResponseDTO buscarPorId (UUID id){
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con el ID: " + id));
        return convertirAEntityADto(mesa);
    }

    // 3. ACTUALIZAR MESA
    public MesaResponseDTO actualizar(UUID id, MesaRequestDTO request) {
        Mesa mesaExistente = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede actualizar, la mesa no existe."));

        // Corregido el nombre de la variable 'request'
        if (!mesaExistente.getNumeroMesa().equalsIgnoreCase(request.getNumeroMesa()) &&
                mesaRepository.existsByNumeroMesa(request.getNumeroMesa())) {
            throw new RuntimeException("Ya existe otra mesa con el número: " + request.getNumeroMesa());
        }

        mesaExistente.setNumeroMesa(request.getNumeroMesa().toUpperCase());
        mesaExistente.setCapacidad(request.getCapacidad());

        Mesa mesaActualizada = mesaRepository.save(mesaExistente);
        return convertirAEntityADto(mesaActualizada);
    }

    // 4. ELIMINAR MESA
    public void eliminar(UUID id) {
        if (!mesaRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar, la mesa no existe.");
        }
        mesaRepository.deleteById(id);
    }

    // Método privado auxiliar (Clean Code: para no repetir el "new MesaResponseDTO" en cada método)
    private MesaResponseDTO convertirAEntityADto(Mesa mesa) {
        return new MesaResponseDTO(
                mesa.getIdMesa(),
                mesa.getNumeroMesa(),
                mesa.getCapacidad(),
                mesa.getEstado().name()
        );
    }
}

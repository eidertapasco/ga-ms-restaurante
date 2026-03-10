package co.edu.sena.ga_ms_restaurante.service;

import co.edu.sena.ga_ms_restaurante.dto.request.MesaRequestDTO;
import co.edu.sena.ga_ms_restaurante.dto.response.MesaResponseDTO;
import co.edu.sena.ga_ms_restaurante.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.model.Mesa;
import co.edu.sena.ga_ms_restaurante.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor //crea un constructor para todos los campos marcados como final
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaResponseDTO crearMesa(MesaRequestDTO requestDTO) {

        // 1. Validar que 'M-1' o 'Mesa-1' ya existe
        if (mesaRepository.existsByNumeroMesa(requestDTO.getNumeroMesa())) {
            throw new IllegalArgumentException("Ya existe una mesa identificada como: " + requestDTO.getNumeroMesa());
        }

        // 2. Construir la entidad
        Mesa nuevaMesa = Mesa.builder()
                .numeroMesa(requestDTO.getNumeroMesa().toUpperCase()) //Guardar todas en mayusculas
                .capacidad(requestDTO.getCapacidad())
                .estado(EstadoMesa.DISPONIBLE)
                .build();

        // 3. Persistir
        Mesa mesaGuardada = mesaRepository.save(nuevaMesa);

        // 4. Retornar el response DTO usando el record
        return new MesaResponseDTO(
                mesaGuardada.getIdMesa(),
                mesaGuardada.getNumeroMesa(),
                mesaGuardada.getCapacidad(),
                mesaGuardada.getEstado().name()
        );
    }
}

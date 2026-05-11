package co.edu.sena.ga_ms_restaurante.mesa.service;

import co.edu.sena.ga_ms_restaurante.exception.custom.BusinessRuleException;
import co.edu.sena.ga_ms_restaurante.exception.custom.ResourceNotFoundException;
import co.edu.sena.ga_ms_restaurante.mesa.dto.request.MesaCreateRequest;
import co.edu.sena.ga_ms_restaurante.mesa.dto.request.MesaUpdateRequest;
import co.edu.sena.ga_ms_restaurante.mesa.dto.response.MesaResponse;
import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.mesa.mapper.MesaMapper;
import co.edu.sena.ga_ms_restaurante.mesa.model.Mesa;
import co.edu.sena.ga_ms_restaurante.mesa.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MesaServiceImpl implements MesaService {

    private final MesaRepository mesaRepository;
    private final MesaMapper mesaMapper;

    @Override
    public List<MesaResponse> listarMesasActivas() {
        return mesaMapper.toResponseList(mesaRepository.findByActivoTrue());
    }

    @Override
    public List<MesaResponse> listarMesasPorEstado(EstadoMesa estado) {
        return mesaMapper.toResponseList(
                mesaRepository.findByActivoTrueAndEstado(estado)
        );
    }

    @Override
    public MesaResponse buscarPorId(UUID id) {
        return mesaMapper.toResponse(obtenerMesaOFallar(id));
    }

    @Override
    public MesaResponse crear(MesaCreateRequest request) {
        if (mesaRepository.existsByNombre(request.getNombre())) {
            throw new BusinessRuleException(
                    "Ya existe una mesa con el nombre: " + request.getNombre()
            );
        }
        Mesa mesa = mesaMapper.toEntity(request);
        return mesaMapper.toResponse(mesaRepository.save(mesa));
    }

    @Override
    public MesaResponse actualizar(UUID id, MesaUpdateRequest request) {
        Mesa mesa = obtenerMesaOFallar(id);

        if (request.getNombre() != null
                && !request.getNombre().equals(mesa.getNombre())
                && mesaRepository.existsByNombre(request.getNombre())) {
            throw new BusinessRuleException(
                    "Ya existe una mesa con el nombre: " + request.getNombre()
            );
        }

        mesaMapper.updateEntity(mesa, request);
        return mesaMapper.toResponse(mesaRepository.save(mesa));
    }

    @Override
    public MesaResponse cambiarEstado(UUID id, EstadoMesa nuevoEstado) {
        Mesa mesa = obtenerMesaOFallar(id);

        if (!mesa.getActivo()) {
            throw new BusinessRuleException(
                    "No se puede cambiar el estado de una mesa inactiva"
            );
        }

        validarTransicionEstado(mesa.getEstado(), nuevoEstado);
        mesa.setEstado(nuevoEstado);
        return mesaMapper.toResponse(mesaRepository.save(mesa));
    }

    @Override
    public MesaResponse activar(UUID id) {
        Mesa mesa = obtenerMesaOFallar(id);

        if (mesa.getActivo()) {
            throw new BusinessRuleException("La mesa ya está activa");
        }

        mesa.setActivo(true);
        mesa.setEstado(EstadoMesa.LIBRE);
        return mesaMapper.toResponse(mesaRepository.save(mesa));
    }

    @Override
    public MesaResponse desactivar(UUID id) {
        Mesa mesa = obtenerMesaOFallar(id);

        if (!mesa.getActivo()) {
            throw new BusinessRuleException("La mesa ya está inactiva");
        }

        if (mesa.getEstado() == EstadoMesa.OCUPADA
                || mesa.getEstado() == EstadoMesa.POR_PAGAR) {
            throw new BusinessRuleException(
                    "No se puede desactivar una mesa que está ocupada o pendiente de pago"
            );
        }

        mesa.setActivo(false);
        mesa.setEstado(EstadoMesa.INACTIVA);
        return mesaMapper.toResponse(mesaRepository.save(mesa));
    }

    // ─── Métodos privados de apoyo ───────────────────────────────────────────

    private Mesa obtenerMesaOFallar(UUID id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mesa no encontrada con id: " + id
                ));
    }

    private void validarTransicionEstado(EstadoMesa actual, EstadoMesa nuevo) {
        boolean transicionValida = switch (actual) {
            case LIBRE      -> nuevo == EstadoMesa.OCUPADA
                    || nuevo == EstadoMesa.INACTIVA;
            case OCUPADA    -> nuevo == EstadoMesa.POR_PAGAR
                    || nuevo == EstadoMesa.LIBRE;
            case POR_PAGAR  -> nuevo == EstadoMesa.LIBRE;
            case INACTIVA   -> false; // se activa por activar(), no por cambiarEstado()
        };

        if (!transicionValida) {
            throw new BusinessRuleException(
                    "Transición de estado inválida: " + actual + " → " + nuevo
            );
        }
    }
}

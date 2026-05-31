package co.edu.sena.ga_ms_restaurante.caja.service;

import co.edu.sena.ga_ms_restaurante.caja.dto.request.AbrirSesionRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.request.CerrarSesionRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.SesionCajaResponse;
import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoSesion;
import co.edu.sena.ga_ms_restaurante.caja.mapper.CajaMapper;
import co.edu.sena.ga_ms_restaurante.caja.model.SesionCaja;
import co.edu.sena.ga_ms_restaurante.caja.repository.SesionCajaRepository;
import co.edu.sena.ga_ms_restaurante.exception.custom.BusinessRuleException;
import co.edu.sena.ga_ms_restaurante.exception.custom.ResourceNotFoundException;
import co.edu.sena.ga_ms_restaurante.security.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CajaServiceImpl implements CajaService {

    private final SesionCajaRepository sesionCajaRepository;
    private final CajaMapper cajaMapper;

    @Override
    @Transactional
    public SesionCajaResponse abrirSesion(AbrirSesionRequest request) {
        if (sesionCajaRepository.existsByEstado(EstadoSesion.ABIERTA)) {
            throw new BusinessRuleException(
                    "Ya existe una sesión de caja abierta. Ciérrala antes de abrir una nueva.");
        }

        SesionCaja sesion = new SesionCaja();
        sesion.setCajeroId(UserContextHolder.getCurrentUserId());
        sesion.setEstado(EstadoSesion.ABIERTA);
        sesion.setBaseEfectivo(request.getBaseEfectivo());
        sesion.setTotalVentasEfectivo(BigDecimal.ZERO);
        sesion.setTotalVentasTarjeta(BigDecimal.ZERO);
        sesion.setTotalVentasTransferencia(BigDecimal.ZERO);

        SesionCaja guardada = sesionCajaRepository.save(sesion);
        log.info("Sesión de caja abierta — id: {}, cajero: {}", guardada.getId(), guardada.getCajeroId());
        return cajaMapper.toSesionResponse(guardada);
    }

    @Override
    @Transactional
    public SesionCajaResponse cerrarSesion(UUID sesionId, CerrarSesionRequest request) {
        SesionCaja sesion = obtenerSesionOFalla(sesionId);

        if (sesion.getEstado() != EstadoSesion.ABIERTA) {
            throw new BusinessRuleException("La sesión ya está cerrada.");
        }

        BigDecimal efectivoReal = request.getEfectivoReal();
        BigDecimal diferencia   = efectivoReal.subtract(
                sesion.getBaseEfectivo().add(sesion.getTotalVentasEfectivo())
        );

        sesion.setEfectivoReal(efectivoReal);
        sesion.setDiferencia(diferencia);
        sesion.setEstado(EstadoSesion.CERRADA);
        sesion.setFechaCierre(LocalDateTime.now());

        log.info("Sesión {} cerrada — diferencia: {}", sesionId, diferencia);
        return cajaMapper.toSesionResponse(sesionCajaRepository.save(sesion));
    }

    @Override
    @Transactional(readOnly = true)
    public SesionCajaResponse buscarSesionPorId(UUID sesionId) {
        return cajaMapper.toSesionResponse(obtenerSesionOFalla(sesionId));
    }

    @Override
    @Transactional(readOnly = true)
    public SesionCajaResponse sesionActiva() {
        SesionCaja sesion = sesionCajaRepository.findByEstado(EstadoSesion.ABIERTA)
                .orElseThrow(() -> new ResourceNotFoundException("No hay sesión de caja abierta."));
        return cajaMapper.toSesionResponse(sesion);
    }

    private SesionCaja obtenerSesionOFalla(UUID id) {
        return sesionCajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sesión de caja no encontrada con id: " + id));
    }
}
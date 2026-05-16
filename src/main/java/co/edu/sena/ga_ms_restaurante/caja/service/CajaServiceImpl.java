package co.edu.sena.ga_ms_restaurante.caja.service;

import co.edu.sena.ga_ms_restaurante.caja.dto.request.AbrirSesionRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.request.CerrarSesionRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.request.FacturarPedidoRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.FacturaResponse;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.SesionCajaResponse;
import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoFactura;
import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoSesion;
import co.edu.sena.ga_ms_restaurante.caja.enums.MetodoPago;
import co.edu.sena.ga_ms_restaurante.caja.mapper.CajaMapper;
import co.edu.sena.ga_ms_restaurante.caja.model.Factura;
import co.edu.sena.ga_ms_restaurante.caja.model.SesionCaja;
import co.edu.sena.ga_ms_restaurante.caja.repository.FacturaRepository;
import co.edu.sena.ga_ms_restaurante.caja.repository.SesionCajaRepository;
import co.edu.sena.ga_ms_restaurante.exception.custom.BusinessRuleException;
import co.edu.sena.ga_ms_restaurante.exception.custom.ResourceNotFoundException;
import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.mesa.model.Mesa;
import co.edu.sena.ga_ms_restaurante.mesa.repository.MesaRepository;
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.pedido.model.Pedido;
import co.edu.sena.ga_ms_restaurante.pedido.repository.PedidoRepository;
import co.edu.sena.ga_ms_restaurante.security.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CajaServiceImpl implements CajaService {

    private final SesionCajaRepository sesionCajaRepository;
    private final FacturaRepository facturaRepository;
    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final CajaMapper cajaMapper;

    // ─── SESIÓN ───────────────────────────────────────────────────────────────

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

    // ─── FACTURACIÓN ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public FacturaResponse facturar(FacturarPedidoRequest request) {

        // 1. Sesión activa
        SesionCaja sesion = sesionCajaRepository.findByEstado(EstadoSesion.ABIERTA)
                .orElseThrow(() -> new BusinessRuleException(
                        "No hay sesión de caja abierta. Abre una sesión antes de facturar."));

        // 2. Pedido existe y está ENTREGADO
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido no encontrado con id: " + request.getPedidoId()));

        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
            throw new BusinessRuleException(
                    "Solo se pueden facturar pedidos en estado ENTREGADO. Estado actual: " + pedido.getEstado());
        }

        // 3. Verificar que no tenga factura previa
        facturaRepository.findByPedido_Id(pedido.getId()).ifPresent(f -> {
            throw new BusinessRuleException(
                    "Este pedido ya fue facturado. Número: " + f.getNumeroFactura());
        });

        // 4. Calcular totales
        BigDecimal subtotal = pedido.getSubtotal();
        BigDecimal propina  = request.getPropina();
        BigDecimal total    = subtotal.add(propina);

        // 5. Crear factura
        Factura factura = new Factura();
        factura.setNumeroFactura(generarNumeroFactura());
        factura.setPedido(pedido);
        factura.setSesionCaja(sesion);
        factura.setCajeroId(UserContextHolder.getCurrentUserId());
        factura.setSubtotal(subtotal);
        factura.setPropina(propina);
        factura.setTotal(total);
        factura.setMetodoPago(request.getMetodoPago());
        factura.setEstado(EstadoFactura.PAGADA);

        // 6. Actualizar acumulados de la sesión
        acumularEnSesion(sesion, request.getMetodoPago(), total);
        sesionCajaRepository.save(sesion);

        // 7. Pedido → FACTURADO, mesa → LIBRE
        pedido.setEstado(EstadoPedido.FACTURADO);
        pedido.setFechaCierre(LocalDateTime.now());
        pedidoRepository.save(pedido);

        Mesa mesa = pedido.getMesa();
        mesa.setEstado(EstadoMesa.LIBRE);
        mesaRepository.save(mesa);

        Factura guardada = facturaRepository.save(factura);
        log.info("Factura {} emitida — pedido: {}, total: {}", guardada.getNumeroFactura(), pedido.getId(), total);

        return cajaMapper.toFacturaResponse(guardada);
    }

    @Override
    @Transactional
    public FacturaResponse anularFactura(UUID facturaId) {

        Factura factura = obtenerFacturaOFalla(facturaId);

        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new BusinessRuleException("La factura ya está anulada.");
        }

        // Revertir acumulados de sesión
        SesionCaja sesion = factura.getSesionCaja();
        if (sesion.getEstado() == EstadoSesion.ABIERTA) {
            revertirEnSesion(sesion, factura.getMetodoPago(), factura.getTotal());
            sesionCajaRepository.save(sesion);
        }

        // Pedido vuelve a ENTREGADO para poder refacturar si aplica
        Pedido pedido = factura.getPedido();
        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setFechaCierre(null);
        pedidoRepository.save(pedido);

        // Mesa vuelve a POR_PAGAR
        Mesa mesa = pedido.getMesa();
        mesa.setEstado(EstadoMesa.POR_PAGAR);
        mesaRepository.save(mesa);

        factura.setEstado(EstadoFactura.ANULADA);
        log.info("Factura {} anulada", factura.getNumeroFactura());

        return cajaMapper.toFacturaResponse(facturaRepository.save(factura));
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponse buscarFacturaPorId(UUID facturaId) {
        return cajaMapper.toFacturaResponse(obtenerFacturaOFalla(facturaId));
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponse buscarFacturaPorNumero(String numeroFactura) {
        Factura factura = facturaRepository.findByNumeroFactura(numeroFactura)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Factura no encontrada con número: " + numeroFactura));
        return cajaMapper.toFacturaResponse(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponse> listarFacturasDeSesion(UUID sesionId) {
        obtenerSesionOFalla(sesionId); // valida que exista
        return cajaMapper.toFacturaResponseList(
                facturaRepository.findBySesionCaja_Id(sesionId));
    }

    // ─── HELPERS PRIVADOS ─────────────────────────────────────────────────────

    private SesionCaja obtenerSesionOFalla(UUID id) {
        return sesionCajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sesión de caja no encontrada con id: " + id));
    }

    private Factura obtenerFacturaOFalla(UUID id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Factura no encontrada con id: " + id));
    }

    private String generarNumeroFactura() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sufijo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "FAC-" + fecha + "-" + sufijo;
    }

    private void acumularEnSesion(SesionCaja sesion, MetodoPago metodo, BigDecimal total) {
        switch (metodo) {
            case EFECTIVO    -> sesion.setTotalVentasEfectivo(
                    sesion.getTotalVentasEfectivo().add(total));
            case TARJETA     -> sesion.setTotalVentasTarjeta(
                    sesion.getTotalVentasTarjeta().add(total));
            case TRANSFERENCIA -> sesion.setTotalVentasTransferencia(
                    sesion.getTotalVentasTransferencia().add(total));
            case CORTESIA    -> { /* no afecta acumulados */ }
        }
    }

    private void revertirEnSesion(SesionCaja sesion, MetodoPago metodo, BigDecimal total) {
        switch (metodo) {
            case EFECTIVO    -> sesion.setTotalVentasEfectivo(
                    sesion.getTotalVentasEfectivo().subtract(total));
            case TARJETA     -> sesion.setTotalVentasTarjeta(
                    sesion.getTotalVentasTarjeta().subtract(total));
            case TRANSFERENCIA -> sesion.setTotalVentasTransferencia(
                    sesion.getTotalVentasTransferencia().subtract(total));
            case CORTESIA    -> { /* nada */ }
        }
    }
}

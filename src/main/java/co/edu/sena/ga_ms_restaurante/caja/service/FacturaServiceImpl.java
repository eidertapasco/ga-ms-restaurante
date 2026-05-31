package co.edu.sena.ga_ms_restaurante.caja.service;

import co.edu.sena.ga_ms_restaurante.caja.dto.request.FacturarPedidoRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.FacturaResponse;
import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoFactura;
import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoSesion;
import co.edu.sena.ga_ms_restaurante.caja.enums.MetodoPago;
import co.edu.sena.ga_ms_restaurante.caja.mapper.FacturaMapper;
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
public class FacturaServiceImpl implements FacturaService {

    private final SesionCajaRepository sesionCajaRepository;
    private final FacturaRepository facturaRepository;
    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final FacturaMapper facturaMapper;

    @Override
    @Transactional
    public FacturaResponse facturar(FacturarPedidoRequest request) {
        SesionCaja sesion = sesionCajaRepository.findByEstado(EstadoSesion.ABIERTA)
                .orElseThrow(() -> new BusinessRuleException(
                        "No hay sesión de caja abierta. Abre una sesión antes de facturar."));

        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido no encontrado con id: " + request.getPedidoId()));

        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
            throw new BusinessRuleException(
                    "Solo se pueden facturar pedidos en estado ENTREGADO. Estado actual: " + pedido.getEstado());
        }

        facturaRepository.findByPedido_Id(pedido.getId()).ifPresent(f -> {
            throw new BusinessRuleException(
                    "Este pedido ya fue facturado. Número: " + f.getNumeroFactura());
        });

        BigDecimal subtotal = pedido.getSubtotal();
        BigDecimal propina  = request.getPropina();
        BigDecimal total    = subtotal.add(propina);

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

        acumularEnSesion(sesion, request.getMetodoPago(), total);
        sesionCajaRepository.save(sesion);

        pedido.setEstado(EstadoPedido.FACTURADO);
        pedido.setFechaCierre(LocalDateTime.now());
        pedidoRepository.save(pedido);

        Mesa mesa = pedido.getMesa();
        mesa.setEstado(EstadoMesa.LIBRE);
        mesaRepository.save(mesa);

        Factura guardada = facturaRepository.save(factura);
        log.info("Factura {} emitida — pedido: {}, total: {}", guardada.getNumeroFactura(), pedido.getId(), total);

        return facturaMapper.toFacturaResponse(guardada);
    }

    @Override
    @Transactional
    public FacturaResponse anularFactura(UUID facturaId) {
        Factura factura = obtenerFacturaOFalla(facturaId);

        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new BusinessRuleException("La factura ya está anulada.");
        }

        SesionCaja sesion = factura.getSesionCaja();
        if (sesion.getEstado() == EstadoSesion.ABIERTA) {
            revertirEnSesion(sesion, factura.getMetodoPago(), factura.getTotal());
            sesionCajaRepository.save(sesion);
        }

        Pedido pedido = factura.getPedido();
        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setFechaCierre(null);
        pedidoRepository.save(pedido);

        Mesa mesa = pedido.getMesa();
        mesa.setEstado(EstadoMesa.POR_PAGAR);
        mesaRepository.save(mesa);

        factura.setEstado(EstadoFactura.ANULADA);
        log.info("Factura {} anulada", factura.getNumeroFactura());

        return facturaMapper.toFacturaResponse(facturaRepository.save(factura));
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponse buscarFacturaPorId(UUID facturaId) {
        return facturaMapper.toFacturaResponse(obtenerFacturaOFalla(facturaId));
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponse buscarFacturaPorNumero(String numeroFactura) {
        Factura factura = facturaRepository.findByNumeroFactura(numeroFactura)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Factura no encontrada con número: " + numeroFactura));
        return facturaMapper.toFacturaResponse(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponse> listarFacturasDeSesion(UUID sesionId) {
        sesionCajaRepository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de caja no encontrada con id: " + sesionId));
        return facturaMapper.toFacturaResponseList(facturaRepository.findBySesionCaja_Id(sesionId));
    }

    private Factura obtenerFacturaOFalla(UUID id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con id: " + id));
    }

    private String generarNumeroFactura() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sufijo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "FAC-" + fecha + "-" + sufijo;
    }

    private void acumularEnSesion(SesionCaja sesion, MetodoPago metodo, BigDecimal total) {
        switch (metodo) {
            case EFECTIVO    -> sesion.setTotalVentasEfectivo(sesion.getTotalVentasEfectivo().add(total));
            case TARJETA     -> sesion.setTotalVentasTarjeta(sesion.getTotalVentasTarjeta().add(total));
            case TRANSFERENCIA -> sesion.setTotalVentasTransferencia(sesion.getTotalVentasTransferencia().add(total));
            case CORTESIA    -> { }
        }
    }

    private void revertirEnSesion(SesionCaja sesion, MetodoPago metodo, BigDecimal total) {
        switch (metodo) {
            case EFECTIVO    -> sesion.setTotalVentasEfectivo(sesion.getTotalVentasEfectivo().subtract(total));
            case TARJETA     -> sesion.setTotalVentasTarjeta(sesion.getTotalVentasTarjeta().subtract(total));
            case TRANSFERENCIA -> sesion.setTotalVentasTransferencia(sesion.getTotalVentasTransferencia().subtract(total));
            case CORTESIA    -> { }
        }
    }
}
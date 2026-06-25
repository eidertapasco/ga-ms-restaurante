package co.edu.sena.ga_ms_restaurante.pedido.service;

import co.edu.sena.ga_ms_restaurante.amqp.dto.CancelacionEvent;
import co.edu.sena.ga_ms_restaurante.amqp.dto.PedidoBarEvent;
import co.edu.sena.ga_ms_restaurante.amqp.dto.PedidoCocinaEvent;
import co.edu.sena.ga_ms_restaurante.amqp.publisher.CancelacionPublisher;
import co.edu.sena.ga_ms_restaurante.amqp.publisher.PedidoBarPublisher;
import co.edu.sena.ga_ms_restaurante.amqp.publisher.PedidoCocinaPublisher;
import co.edu.sena.ga_ms_restaurante.exception.custom.BusinessRuleException;
import co.edu.sena.ga_ms_restaurante.exception.custom.ResourceNotFoundException;
import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.mesa.model.Mesa;
import co.edu.sena.ga_ms_restaurante.mesa.repository.MesaRepository;
import co.edu.sena.ga_ms_restaurante.pedido.dto.request.PedidoCreateRequest;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResumenResponse;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResponse;
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoDetallePedido;
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.pedido.incidencia.dto.response.IncidenciaPedidoResponse;
import co.edu.sena.ga_ms_restaurante.pedido.incidencia.enums.EstadoIncidencia;
import co.edu.sena.ga_ms_restaurante.pedido.incidencia.enums.TipoIncidencia;
import co.edu.sena.ga_ms_restaurante.pedido.incidencia.model.IncidenciaPedido;
import co.edu.sena.ga_ms_restaurante.pedido.incidencia.repository.IncidenciaPedidoRepository;
import co.edu.sena.ga_ms_restaurante.pedido.mapper.PedidoMapper;
import co.edu.sena.ga_ms_restaurante.pedido.model.DetallePedido;
import co.edu.sena.ga_ms_restaurante.pedido.model.Pedido;
import co.edu.sena.ga_ms_restaurante.pedido.repository.DetallePedidoRepository;
import co.edu.sena.ga_ms_restaurante.pedido.repository.PedidoRepository;
import co.edu.sena.ga_ms_restaurante.security.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private static final Set<EstadoPedido> ESTADOS_PEDIDO_CANCELABLE =
            Set.of(EstadoPedido.BORRADOR, EstadoPedido.ENVIADO_COCINA, EstadoPedido.EN_PREPARACION);

    private static final Set<EstadoPedido> ESTADOS_PEDIDO_DEVOLVIBLE =
            Set.of(EstadoPedido.LISTO_PARA_SERVIR, EstadoPedido.ENTREGADO);

    private static final Set<EstadoDetallePedido> ESTADOS_ITEM_CANCELABLE =
            Set.of(EstadoDetallePedido.PENDIENTE, EstadoDetallePedido.PREPARANDO);

    private final PedidoRepository           pedidoRepository;
    private final MesaRepository             mesaRepository;
    private final PedidoMapper               pedidoMapper;
    private final PedidoCocinaPublisher      cocinaPublisher;
    private final PedidoBarPublisher         barPublisher;
    private final DetallePedidoRepository    detallePedidoRepository;
    private final CancelacionPublisher       cancelacionPublisher;
    private final IncidenciaPedidoRepository incidenciaPedidoRepository;

    // ─── CREAR ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PedidoResponse crear(PedidoCreateRequest request) {

        Mesa mesa = mesaRepository.findById(request.getMesaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mesa no encontrada con id: " + request.getMesaId()));

        if (mesa.getEstado() != EstadoMesa.LIBRE) {
            throw new BusinessRuleException(
                    "La mesa '" + mesa.getNombre() + "' no está disponible. Estado actual: "
                            + mesa.getEstado());
        }

        Pedido pedido = new Pedido();
        pedido.setMesa(mesa);
        pedido.setMeseroId(UserContextHolder.getCurrentUserId());
        pedido.setNumeroComensales(request.getNumeroComensales());
        pedido.setNotas(request.getNotas());
        pedido.setEstado(EstadoPedido.BORRADOR);

        List<DetallePedido> detalles = request.getDetalles().stream()
                .map(req -> {
                    DetallePedido d = pedidoMapper.toDetalleEntity(req);
                    d.setPedido(pedido);
                    return d;
                }).toList();

        pedido.setDetalles(detalles);
        pedido.setSubtotal(calcularSubtotal(detalles));

        mesa.setEstado(EstadoMesa.OCUPADA);
        mesaRepository.save(mesa);

        Pedido guardado = pedidoRepository.save(pedido);
        log.info("Pedido creado: {} — mesa: {}", guardado.getId(), mesa.getNombre());

        return construirRespuesta(guardado);
    }

    // ─── CONFIRMAR Y ENVIAR A COCINA/BAR ─────────────────────────────────────

    @Override
    @Transactional
    public PedidoResponse confirmarYEnviar(UUID pedidoId) {

        Pedido pedido = obtenerPedidoOFalla(pedidoId);

        if (pedido.getEstado() != EstadoPedido.BORRADOR) {
            throw new BusinessRuleException(
                    "Solo se pueden confirmar pedidos en estado BORRADOR. Estado actual: "
                            + pedido.getEstado());
        }

        validarPropietarioOInstructor(pedido);

        List<PedidoCocinaEvent.Item> itemsCocina = pedido.getDetalles().stream()
                .filter(d -> "COMIDA".equalsIgnoreCase(d.getCategoria()))
                .map(d -> new PedidoCocinaEvent.Item(
                        d.getId().toString(),
                        d.getProductoId(),
                        d.getNombreProducto(),
                        d.getCantidad(),
                        d.getObservaciones()))
                .toList();

        List<PedidoBarEvent.Item> itemsBar = pedido.getDetalles().stream()
                .filter(d -> "BEBIDA".equalsIgnoreCase(d.getCategoria()))
                .map(d -> new PedidoBarEvent.Item(
                        d.getId().toString(),
                        d.getProductoId(),
                        d.getNombreProducto(),
                        d.getCantidad(),
                        d.getObservaciones()))
                .toList();

        if (!itemsCocina.isEmpty()) {
            cocinaPublisher.publicar(pedido, itemsCocina);
        }

        if (!itemsBar.isEmpty()) {
            barPublisher.publicar(pedido, itemsBar);
        }

        pedido.setEstado(EstadoPedido.ENVIADO_COCINA);
        return construirRespuesta(pedidoRepository.save(pedido));
    }

    // ─── MARCAR ENTREGADO ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public PedidoResponse marcarEntregado(UUID pedidoId) {

        Pedido pedido = obtenerPedidoOFalla(pedidoId);

        if (pedido.getEstado() != EstadoPedido.LISTO_PARA_SERVIR) {
            throw new BusinessRuleException(
                    "Solo se pueden entregar pedidos en estado LISTO_PARA_SERVIR. Estado actual: "
                            + pedido.getEstado());
        }

        validarPropietarioOInstructor(pedido);

        pedido.setEstado(EstadoPedido.ENTREGADO);

        Mesa mesa = pedido.getMesa();
        mesa.setEstado(EstadoMesa.POR_PAGAR);
        mesaRepository.save(mesa);

        log.info("Pedido {} marcado como ENTREGADO — mesa {} pasa a POR_PAGAR",
                pedidoId, mesa.getNombre());
        return construirRespuesta(pedidoRepository.save(pedido));
    }

    // ─── CANCELAR (global) ────────────────────────────────────────────────────
    // Solo se permite mientras el pedido no tenga ningún ítem ya preparado.

    @Override
    @Transactional
    public PedidoResponse cancelar(UUID pedidoId, String motivo) {

        Pedido pedido = obtenerPedidoOFalla(pedidoId);
        EstadoPedido estadoActual = pedido.getEstado();

        if (!ESTADOS_PEDIDO_CANCELABLE.contains(estadoActual)) {
            throw new BusinessRuleException(
                    "No se puede cancelar un pedido en estado: " + estadoActual);
        }

        validarPropietarioOInstructor(pedido);

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFechaCierre(LocalDateTime.now());
        pedido.getDetalles().forEach(d -> d.setEstadoDetalle(EstadoDetallePedido.CANCELADO));
        detallePedidoRepository.saveAll(pedido.getDetalles());

        if (estadoActual != EstadoPedido.BORRADOR) {
            Mesa mesa = pedido.getMesa();
            mesa.setEstado(EstadoMesa.LIBRE);
            mesaRepository.save(mesa);
            log.info("Mesa {} liberada por cancelación del pedido {}", mesa.getNombre(), pedidoId);
            notificarCancelacionGlobal(pedido, false, motivo);
        }

        registrarIncidencia(pedido, null, TipoIncidencia.CANCELACION, null, motivo, null);

        return construirRespuesta(pedidoRepository.save(pedido));
    }

    // ─── DEVOLVER (global) ────────────────────────────────────────────────────
    // Solo se permite cuando el pedido ya está LISTO_PARA_SERVIR o ENTREGADO.
    // No cierra el pedido ni libera la mesa: sigue activo mientras se reprocesa.

    @Override
    @Transactional
    public PedidoResponse devolverGlobal(UUID pedidoId, String motivo) {

        Pedido pedido = obtenerPedidoOFalla(pedidoId);
        EstadoPedido estadoActual = pedido.getEstado();

        if (!ESTADOS_PEDIDO_DEVOLVIBLE.contains(estadoActual)) {
            throw new BusinessRuleException(
                    "No se puede devolver un pedido en estado: " + estadoActual);
        }

        validarPropietarioOInstructor(pedido);

        pedido.getDetalles().stream()
                .filter(d -> d.getEstadoDetalle() != EstadoDetallePedido.CANCELADO)
                .forEach(d -> d.setEstadoDetalle(EstadoDetallePedido.EN_DEVOLUCION));
        detallePedidoRepository.saveAll(pedido.getDetalles());
        pedido.setEstado(EstadoPedido.EN_DEVOLUCION);

        notificarCancelacionGlobal(pedido, true, motivo);
        registrarIncidencia(pedido, null, TipoIncidencia.DEVOLUCION, null, motivo, estadoActual);

        log.info("Pedido {} en devolución — mesa sigue ocupada", pedidoId);
        return construirRespuesta(pedidoRepository.save(pedido));
    }

    // ─── CANCELAR ÍTEM (parcial o total) ──────────────────────────────────────
    // Solo se permite mientras el ítem está PENDIENTE o PREPARANDO.

    @Override
    @Transactional
    public PedidoResponse cancelarDetalle(UUID detalleId, String motivo, Integer cantidad) {

        DetallePedido detalle = obtenerDetalleOFalla(detalleId);
        Pedido pedido = detalle.getPedido();

        if (pedido.getEstado() == EstadoPedido.FACTURADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new BusinessRuleException(
                    "No se puede cancelar un ítem de un pedido en estado: " + pedido.getEstado());
        }
        if (!ESTADOS_ITEM_CANCELABLE.contains(detalle.getEstadoDetalle())) {
            throw new BusinessRuleException(
                    "No se puede cancelar un ítem en estado: " + detalle.getEstadoDetalle());
        }

        validarPropietarioOInstructor(pedido);

        int activa    = detalle.getCantidad();
        int aCancelar = (cantidad != null) ? cantidad : activa;

        if (aCancelar <= 0 || aCancelar > activa) {
            throw new BusinessRuleException(
                    "Cantidad inválida: " + aCancelar + " (unidades activas: " + activa + ")");
        }

        if (aCancelar == activa) {
            detalle.setEstadoDetalle(EstadoDetallePedido.CANCELADO);
        } else {
            int restantes = activa - aCancelar;
            detalle.setCantidad(restantes);
            detalle.setSubtotalLinea(
                    detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(restantes)));
        }
        detallePedidoRepository.save(detalle);

        recalcularSubtotal(pedido);
        pedidoRepository.save(pedido);

        notificarCancelacionDetalle(detalle, false, motivo, aCancelar);
        registrarIncidencia(pedido, detalle, TipoIncidencia.CANCELACION, aCancelar, motivo, null);

        log.info("Ítem {} — canceladas {} de {} unidades (pedido {})",
                detalleId, aCancelar, activa, pedido.getId());

        boolean todosCancelados = detallePedidoRepository
                .findByPedido_Id(pedido.getId())
                .stream()
                .allMatch(d -> d.getEstadoDetalle() == EstadoDetallePedido.CANCELADO);

        if (todosCancelados) {
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedido.setFechaCierre(LocalDateTime.now());
            pedido.getMesa().setEstado(EstadoMesa.LIBRE);
            mesaRepository.save(pedido.getMesa());
            pedidoRepository.save(pedido);
        }

        return construirRespuesta(obtenerPedidoOFalla(pedido.getId()));
    }

    // ─── DEVOLVER ÍTEM (parcial o total) ──────────────────────────────────────
    // Solo se permite cuando el ítem está TERMINADO. No se resta del subtotal:
    // sigue siendo parte del pedido mientras Cocina/Bar lo reprocesan (Bloque 3).

    @Override
    @Transactional
    public PedidoResponse devolverDetalle(UUID detalleId, String motivo, Integer cantidad) {

        DetallePedido detalle = obtenerDetalleOFalla(detalleId);
        Pedido pedido = detalle.getPedido();

        if (pedido.getEstado() == EstadoPedido.FACTURADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new BusinessRuleException(
                    "No se puede devolver un ítem de un pedido en estado: " + pedido.getEstado());
        }
        if (detalle.getEstadoDetalle() != EstadoDetallePedido.TERMINADO) {
            throw new BusinessRuleException(
                    "Solo se puede devolver un ítem TERMINADO. Estado actual: " + detalle.getEstadoDetalle());
        }

        validarPropietarioOInstructor(pedido);

        int activa    = detalle.getCantidad();
        int aDevolver = (cantidad != null) ? cantidad : activa;

        if (aDevolver <= 0 || aDevolver > activa) {
            throw new BusinessRuleException(
                    "Cantidad inválida: " + aDevolver + " (unidades activas: " + activa + ")");
        }

        detalle.setEstadoDetalle(EstadoDetallePedido.EN_DEVOLUCION);
        detallePedidoRepository.save(detalle);

        EstadoPedido estadoPrevio = pedido.getEstado();
        boolean primeraDevolucionAbierta = estadoPrevio != EstadoPedido.EN_DEVOLUCION;
        if (primeraDevolucionAbierta) {
            pedido.setEstado(EstadoPedido.EN_DEVOLUCION);
        }
        pedidoRepository.save(pedido);

        notificarCancelacionDetalle(detalle, true, motivo, aDevolver);
        registrarIncidencia(pedido, detalle, TipoIncidencia.DEVOLUCION, aDevolver, motivo,
                primeraDevolucionAbierta ? estadoPrevio : null);

        log.info("Ítem {} — devueltas {} de {} unidades, sigue facturándose (pedido {})",
                detalleId, aDevolver, activa, pedido.getId());

        return construirRespuesta(obtenerPedidoOFalla(pedido.getId()));
    }

    // ─── CONSULTAS ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(UUID id) {
        return construirRespuesta(obtenerPedidoOFalla(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResumenResponse> listarPorMesero(UUID meseroId) {
        return pedidoMapper.toResumenList(pedidoRepository.findByMeseroId(meseroId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResumenResponse> listarTodos() {
        return pedidoMapper.toResumenList(pedidoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResumenResponse> listarPorEstado(EstadoPedido estado) {
        return pedidoMapper.toResumenList(pedidoRepository.findByEstado(estado));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResumenResponse> listarPorMesa(UUID mesaId) {
        return pedidoMapper.toResumenList(pedidoRepository.findByMesa_Id(mesaId));
    }

    // ─── LISTENER RabbitMQ ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void actualizarEstadoDesdeEvento(UUID pedidoId, EstadoPedido nuevoEstado) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido no encontrado con id: " + pedidoId));

        EstadoPedido estadoActual = pedido.getEstado();

        boolean transicionValida = switch (nuevoEstado) {
            case EN_PREPARACION    -> estadoActual == EstadoPedido.ENVIADO_COCINA;
            case LISTO_PARA_SERVIR -> estadoActual == EstadoPedido.EN_PREPARACION;
            case CANCELADO         -> estadoActual != EstadoPedido.FACTURADO
                    && estadoActual != EstadoPedido.CANCELADO;
            default -> false;
        };

        if (!transicionValida) {
            log.warn("Transición ignorada: {} → {} para pedido {}",
                    estadoActual, nuevoEstado, pedidoId);
            return;
        }

        pedido.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoPedido.CANCELADO) {
            pedido.setFechaCierre(LocalDateTime.now());
            Mesa mesa = pedido.getMesa();
            mesa.setEstado(EstadoMesa.LIBRE);
            mesaRepository.save(mesa);
        }

        pedidoRepository.save(pedido);
        log.info("Pedido {} actualizado a {} por evento RabbitMQ", pedidoId, nuevoEstado);
    }

    @Override
    @Transactional
    public void actualizarEstadoDetalleDesdeEvento(UUID detalleId, EstadoDetallePedido nuevoEstado) {

        DetallePedido detalle = detallePedidoRepository.findById(detalleId).orElse(null);
        if (detalle == null) {
            log.warn("Ítem {} no encontrado al procesar evento de plato — ignorado", detalleId);
            return;
        }

        EstadoDetallePedido estadoAnterior = detalle.getEstadoDetalle();
        detalle.setEstadoDetalle(nuevoEstado);
        detallePedidoRepository.save(detalle);

        if (estadoAnterior == EstadoDetallePedido.EN_DEVOLUCION
                && nuevoEstado == EstadoDetallePedido.TERMINADO) {
            cerrarDevolucionResuelta(detalle);
        }
    }

    /** Cierra la incidencia del ítem y, si ya no quedan devoluciones abiertas, regresa el pedido a su estado anterior. */
    private void cerrarDevolucionResuelta(DetallePedido detalle) {
        incidenciaPedidoRepository.findByDetalle_IdAndEstado(detalle.getId(), EstadoIncidencia.EN_PROCESO)
                .ifPresent(incidencia -> {
                    incidencia.setEstado(EstadoIncidencia.RESUELTA);
                    incidencia.setFechaResolucion(LocalDateTime.now());
                    incidenciaPedidoRepository.save(incidencia);

                    Pedido pedido = detalle.getPedido();
                    boolean quedanAbiertas = pedido.getDetalles().stream()
                            .anyMatch(d -> d.getEstadoDetalle() == EstadoDetallePedido.EN_DEVOLUCION);

                    if (!quedanAbiertas && pedido.getEstado() == EstadoPedido.EN_DEVOLUCION) {
                        EstadoPedido estadoARestaurar = incidenciaPedidoRepository
                                .findFirstByPedido_IdAndEstadoPedidoPrevioIsNotNullOrderByFechaRegistroDesc(pedido.getId())
                                .map(IncidenciaPedido::getEstadoPedidoPrevio)
                                .orElseGet(() -> {
                                    log.warn("Pedido {} sin estadoPedidoPrevio registrado — se asume ENTREGADO", pedido.getId());
                                    return EstadoPedido.ENTREGADO;
                                });
                        pedido.setEstado(estadoARestaurar);
                        pedidoRepository.save(pedido);
                        log.info("Pedido {} sale de EN_DEVOLUCION → {}", pedido.getId(), estadoARestaurar);
                    }
                });
    }

    // ─── HELPERS PRIVADOS ─────────────────────────────────────────────────────

    private Pedido obtenerPedidoOFalla(UUID id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido no encontrado con id: " + id));
    }

    private DetallePedido obtenerDetalleOFalla(UUID id) {
        return detallePedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ítem de pedido no encontrado con id: " + id));
    }

    private BigDecimal calcularSubtotal(List<DetallePedido> detalles) {
        return detalles.stream()
                .map(DetallePedido::getSubtotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Excluye solo CANCELADO/DEVUELTO. EN_DEVOLUCION sigue contando — Bloque 3. */
    private void recalcularSubtotal(Pedido pedido) {
        BigDecimal nuevo = pedido.getDetalles().stream()
                .filter(d -> d.getEstadoDetalle() != EstadoDetallePedido.CANCELADO
                        && d.getEstadoDetalle() != EstadoDetallePedido.DEVUELTO)
                .map(DetallePedido::getSubtotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setSubtotal(nuevo);
    }

    private PedidoResponse construirRespuesta(Pedido pedido) {
        List<IncidenciaPedidoResponse> incidencias = incidenciaPedidoRepository
                .findByPedido_IdOrderByFechaRegistroDesc(pedido.getId())
                .stream()
                .map(pedidoMapper::toIncidenciaResponse)
                .toList();
        return pedidoMapper.toResponse(pedido, incidencias);
    }

    /**
     * Registra el historial de una cancelación/devolución. Las cancelaciones
     * quedan RESUELTA de inmediato (no hay nada más que esperar). Las
     * devoluciones quedan EN_PROCESO — el siguiente bloque (listener de
     * Cocina/Bar) las cierra cuando el ítem vuelve a estar TERMINADO.
     */
    private void registrarIncidencia(Pedido pedido, DetallePedido detalle, TipoIncidencia tipo,
                                     Integer cantidad, String motivo, EstadoPedido estadoPrevio) {
        IncidenciaPedido inc = new IncidenciaPedido();
        inc.setPedido(pedido);
        inc.setDetalle(detalle);
        inc.setNombreProductoSnapshot(detalle != null ? detalle.getNombreProducto() : null);
        inc.setTipo(tipo);
        inc.setCantidadAfectada(cantidad);
        inc.setMotivo(motivo);
        inc.setRegistradaPor(UserContextHolder.getCurrentUserId());
        inc.setEstadoPedidoPrevio(estadoPrevio);

        if (tipo == TipoIncidencia.CANCELACION) {
            inc.setEstado(EstadoIncidencia.RESUELTA);
            inc.setFechaResolucion(LocalDateTime.now());
        } else {
            inc.setEstado(EstadoIncidencia.EN_PROCESO);
        }
        incidenciaPedidoRepository.save(inc);
    }

    private void validarPropietarioOInstructor(Pedido pedido) {
        UUID currentUserId = UserContextHolder.getCurrentUserId();
        String currentRole = UserContextHolder.getCurrentUserRole();

        boolean esPropietario = pedido.getMeseroId().equals(currentUserId);
        boolean esInstructor  = "INSTRUCTOR".equalsIgnoreCase(currentRole)
                || "ADMIN".equalsIgnoreCase(currentRole);

        if (!esPropietario && !esInstructor) {
            throw new BusinessRuleException(
                    "No tienes permiso para modificar este pedido");
        }
    }

    private void notificarCancelacionDetalle(DetallePedido detalle, boolean devolucion,
                                             String motivo, Integer cantidad) {
        CancelacionEvent evento = new CancelacionEvent(
                detalle.getPedido().getId(), detalle.getId(), devolucion, motivo, cantidad);

        String categoria = detalle.getCategoria();
        if ("COMIDA".equalsIgnoreCase(categoria)) {
            cancelacionPublisher.publicarACocina(evento);
        } else if ("BEBIDA".equalsIgnoreCase(categoria)) {
            cancelacionPublisher.publicarABar(evento);
        } else {
            log.warn("Categoría desconocida '{}' en detalle {} — no se notifica cancelación",
                    categoria, detalle.getId());
        }
    }

    private void notificarCancelacionGlobal(Pedido pedido, boolean devolucion, String motivo) {
        CancelacionEvent evento = new CancelacionEvent(
                pedido.getId(), null, devolucion, motivo, null);

        boolean hayComida = pedido.getDetalles().stream()
                .anyMatch(d -> "COMIDA".equalsIgnoreCase(d.getCategoria()));
        boolean hayBebida = pedido.getDetalles().stream()
                .anyMatch(d -> "BEBIDA".equalsIgnoreCase(d.getCategoria()));

        if (hayComida) cancelacionPublisher.publicarACocina(evento);
        if (hayBebida) cancelacionPublisher.publicarABar(evento);
    }
}
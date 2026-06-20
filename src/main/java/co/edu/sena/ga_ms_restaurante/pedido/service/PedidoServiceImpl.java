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
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository        pedidoRepository;
    private final MesaRepository          mesaRepository;
    private final PedidoMapper            pedidoMapper;
    private final PedidoCocinaPublisher   cocinaPublisher;
    private final PedidoBarPublisher      barPublisher;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CancelacionPublisher    cancelacionPublisher;

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

        return pedidoMapper.toResponse(guardado);
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

        // Separar ítems por categoría directamente desde las entidades DetallePedido
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

        // El adapter construye el ComandaRequestDTO completo — logs dentro de cada publisher
        if (!itemsCocina.isEmpty()) {
            cocinaPublisher.publicar(pedido, itemsCocina);
        }

        if (!itemsBar.isEmpty()) {
            barPublisher.publicar(pedido, itemsBar);
        }

        pedido.setEstado(EstadoPedido.ENVIADO_COCINA);
        return pedidoMapper.toResponse(pedidoRepository.save(pedido));
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
        return pedidoMapper.toResponse(pedidoRepository.save(pedido));
    }

    // ─── CANCELAR (global) ────────────────────────────────────────────────────

    @Override
    @Transactional
    public PedidoResponse cancelar(UUID pedidoId, String motivo) {

        Pedido pedido = obtenerPedidoOFalla(pedidoId);

        EstadoPedido estadoActual = pedido.getEstado();
        if (estadoActual == EstadoPedido.FACTURADO || estadoActual == EstadoPedido.CANCELADO) {
            throw new BusinessRuleException(
                    "No se puede cancelar un pedido en estado: " + estadoActual);
        }

        validarPropietarioOInstructor(pedido);

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFechaCierre(LocalDateTime.now());

        if (estadoActual != EstadoPedido.BORRADOR) {
            Mesa mesa = pedido.getMesa();
            mesa.setEstado(EstadoMesa.LIBRE);
            mesaRepository.save(mesa);
            log.info("Mesa {} liberada por cancelación del pedido {}", mesa.getNombre(), pedidoId);

            // Notificar a Cocina y Bar solo si el pedido ya había sido enviado
            notificarCancelacionGlobal(pedido, false, motivo);
        }

        return pedidoMapper.toResponse(pedidoRepository.save(pedido));
    }

    // ─── DEVOLVER (global) ────────────────────────────────────────────────────

    @Override
    @Transactional
    public PedidoResponse devolverGlobal(UUID pedidoId, String motivo) {

        Pedido pedido = obtenerPedidoOFalla(pedidoId);

        EstadoPedido estadoActual = pedido.getEstado();
        if (estadoActual == EstadoPedido.FACTURADO
                || estadoActual == EstadoPedido.CANCELADO
                || estadoActual == EstadoPedido.BORRADOR) {
            throw new BusinessRuleException(
                    "No se puede devolver un pedido en estado: " + estadoActual);
        }

        validarPropietarioOInstructor(pedido);

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFechaCierre(LocalDateTime.now());

        Mesa mesa = pedido.getMesa();
        mesa.setEstado(EstadoMesa.LIBRE);
        mesaRepository.save(mesa);

        notificarCancelacionGlobal(pedido, true, motivo);

        log.info("Pedido {} devuelto (global) — mesa {} liberada", pedidoId, mesa.getNombre());
        return pedidoMapper.toResponse(pedidoRepository.save(pedido));
    }

    // ─── CANCELAR ÍTEM (parcial o total) ──────────────────────────────────────

    @Override
    @Transactional
    public PedidoResponse cancelarDetalle(UUID detalleId, String motivo, Integer cantidad) {

        DetallePedido detalle = obtenerDetalleOFalla(detalleId);
        Pedido pedido = detalle.getPedido();

        EstadoPedido estadoActual = pedido.getEstado();
        if (estadoActual == EstadoPedido.FACTURADO
                || estadoActual == EstadoPedido.CANCELADO
                || estadoActual == EstadoPedido.BORRADOR) {
            throw new BusinessRuleException(
                    "No se puede cancelar un ítem de un pedido en estado: " + estadoActual);
        }

        validarPropietarioOInstructor(pedido);

        int activa    = detalle.getCantidad();
        int aCancelar = (cantidad != null) ? cantidad : activa;   // null = todas

        if (aCancelar <= 0 || aCancelar > activa) {
            throw new BusinessRuleException(
                    "Cantidad inválida: " + aCancelar + " (unidades activas: " + activa + ")");
        }

        if (aCancelar == activa) {
            // Cancelación total del ítem (comportamiento de siempre)
            detalle.setEstadoDetalle("CANCELADO");
        } else {
            // Cancelación parcial: se descuentan unidades y se recalcula la línea
            int restantes = activa - aCancelar;
            detalle.setCantidad(restantes);
            detalle.setSubtotalLinea(
                    detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(restantes)));
        }
        detallePedidoRepository.save(detalle);

        // Recalcular el subtotal del pedido para que la factura no cobre lo cancelado
        recalcularSubtotal(pedido);
        pedidoRepository.save(pedido);

        // Notificar a Cocina/Bar (según categoría) con la cantidad cancelada
        notificarCancelacionDetalle(detalle, false, motivo, aCancelar);

        log.info("Ítem {} — canceladas {} de {} unidades (pedido {})",
                detalleId, aCancelar, activa, pedido.getId());

        // Si todos los ítems quedaron cancelados → cerrar el pedido completo
        boolean todosCancelados = detallePedidoRepository
                .findByPedido_Id(pedido.getId())
                .stream()
                .allMatch(d -> "CANCELADO".equals(d.getEstadoDetalle()));

        if (todosCancelados) {
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedido.setFechaCierre(LocalDateTime.now());
            pedido.getMesa().setEstado(EstadoMesa.LIBRE);
            mesaRepository.save(pedido.getMesa());
            pedidoRepository.save(pedido);
            log.info("Todos los ítems cancelados — pedido {} cerrado automáticamente",
                    pedido.getId());
        }

        return pedidoMapper.toResponse(obtenerPedidoOFalla(pedido.getId()));
    }

    // ─── DEVOLVER ÍTEM (parcial o total) ──────────────────────────────────────

    @Override
    @Transactional
    public PedidoResponse devolverDetalle(UUID detalleId, String motivo, Integer cantidad) {

        DetallePedido detalle = obtenerDetalleOFalla(detalleId);
        Pedido pedido = detalle.getPedido();

        if (pedido.getEstado() == EstadoPedido.FACTURADO
                || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new BusinessRuleException(
                    "No se puede devolver un ítem de un pedido en estado: " + pedido.getEstado());
        }

        validarPropietarioOInstructor(pedido);

        int activa    = detalle.getCantidad();
        int aDevolver = (cantidad != null) ? cantidad : activa;   // null = todas

        if (aDevolver <= 0 || aDevolver > activa) {
            throw new BusinessRuleException(
                    "Cantidad inválida: " + aDevolver + " (unidades activas: " + activa + ")");
        }

        if (aDevolver == activa) {
            // Devolución total del ítem
            detalle.setEstadoDetalle("DEVUELTO");
        } else {
            // Devolución parcial: se descuentan unidades y se recalcula la línea
            int restantes = activa - aDevolver;
            detalle.setCantidad(restantes);
            detalle.setSubtotalLinea(
                    detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(restantes)));
        }
        detallePedidoRepository.save(detalle);

        recalcularSubtotal(pedido);
        pedidoRepository.save(pedido);

        notificarCancelacionDetalle(detalle, true, motivo, aDevolver);

        log.info("Ítem {} — devueltas {} de {} unidades (pedido {})",
                detalleId, aDevolver, activa, pedido.getId());
        return pedidoMapper.toResponse(obtenerPedidoOFalla(pedido.getId()));
    }

    // ─── CONSULTAS ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(UUID id) {
        return pedidoMapper.toResponse(obtenerPedidoOFalla(id));
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

    /**
     * Recalcula el subtotal del pedido sumando solo las líneas vigentes,
     * es decir, excluyendo los ítems totalmente cancelados o devueltos.
     * Así la factura nunca cobra unidades que ya no se sirvieron.
     */
    private void recalcularSubtotal(Pedido pedido) {
        BigDecimal nuevo = pedido.getDetalles().stream()
                .filter(d -> !"CANCELADO".equalsIgnoreCase(d.getEstadoDetalle())
                        && !"DEVUELTO".equalsIgnoreCase(d.getEstadoDetalle()))
                .map(DetallePedido::getSubtotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setSubtotal(nuevo);
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

    /**
     * Enruta la cancelación/devolución de un ítem puntual al módulo correcto
     * según su categoría (COMIDA → Cocina, BEBIDA → Bar), incluyendo la cantidad.
     */
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

    /**
     * Enruta una cancelación/devolución global solo a los módulos que realmente
     * tengan ítems de su categoría en el pedido (mismo criterio que la confirmación).
     * cantidad = null → afecta todas las unidades activas de cada ítem.
     */
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
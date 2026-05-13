package co.edu.sena.ga_ms_restaurante.pedido.service;

import co.edu.sena.ga_ms_restaurante.amqp.dto.PedidoBarEvent;
import co.edu.sena.ga_ms_restaurante.amqp.dto.PedidoCocinaEvent;
import co.edu.sena.ga_ms_restaurante.amqp.publisher.PedidoBarPublisher;
import co.edu.sena.ga_ms_restaurante.amqp.publisher.PedidoCocinaPublisher;
import co.edu.sena.ga_ms_restaurante.exception.custom.BusinessRuleException;
import co.edu.sena.ga_ms_restaurante.exception.custom.ResourceNotFoundException;
import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.mesa.model.Mesa;
import co.edu.sena.ga_ms_restaurante.mesa.repository.MesaRepository;
import co.edu.sena.ga_ms_restaurante.pedido.dto.request.DetallePedidoRequest;
import co.edu.sena.ga_ms_restaurante.pedido.dto.request.PedidoCreateRequest;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResumenResponse;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResponse;
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.pedido.mapper.PedidoMapper;
import co.edu.sena.ga_ms_restaurante.pedido.model.DetallePedido;
import co.edu.sena.ga_ms_restaurante.pedido.model.Pedido;
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

    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final PedidoMapper pedidoMapper;
    private final PedidoCocinaPublisher cocinaPublisher;
    private final PedidoBarPublisher barPublisher;

    // ─── CREAR ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PedidoResponse crear(PedidoCreateRequest request) {

        // 1. Verificar que la mesa existe y está LIBRE
        Mesa mesa = mesaRepository.findById(request.getMesaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mesa no encontrada con id: " + request.getMesaId()));

        if (mesa.getEstado() != EstadoMesa.LIBRE) {
            throw new BusinessRuleException(
                    "La mesa '" + mesa.getNombre() + "' no está disponible. Estado actual: " + mesa.getEstado());
        }

        // 2. Construir el pedido
        Pedido pedido = new Pedido();
        pedido.setMesa(mesa);
        pedido.setMeseroId(UserContextHolder.getCurrentUserId());
        pedido.setNumeroComensales(request.getNumeroComensales());
        pedido.setNotas(request.getNotas());
        pedido.setEstado(EstadoPedido.BORRADOR);

        // 3. Construir detalles y calcular subtotales
        List<DetallePedido> detalles = request.getDetalles().stream()
                .map(req -> {
                    DetallePedido d = pedidoMapper.toDetalleEntity(req);
                    d.setPedido(pedido);
                    return d;
                }).toList();

        pedido.setDetalles(detalles);
        pedido.setSubtotal(calcularSubtotal(detalles));

        // 4. Cambiar estado de mesa a OCUPADA
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
                    "Solo se pueden confirmar pedidos en estado BORRADOR. Estado actual: " + pedido.getEstado());
        }

        validarPropietarioOInstructor(pedido);

        // Separar ítems por categoría
        List<DetallePedidoRequest> todosLosItems = pedido.getDetalles().stream()
                .map(this::detalleToRequest)
                .toList();

        List<PedidoCocinaEvent.Item> itemsCocina = todosLosItems.stream()
                .filter(d -> "COMIDA".equalsIgnoreCase(d.getCategoria()))
                .map(d -> new PedidoCocinaEvent.Item(
                        d.getProductoId(), d.getNombreProducto(), d.getCantidad(), d.getObservaciones()))
                .toList();

        List<PedidoBarEvent.Item> itemsBar = todosLosItems.stream()
                .filter(d -> "BEBIDA".equalsIgnoreCase(d.getCategoria()))
                .map(d -> new PedidoBarEvent.Item(
                        d.getProductoId(), d.getNombreProducto(), d.getCantidad(), d.getObservaciones()))
                .toList();

        // Publicar eventos solo si hay ítems de esa categoría
        if (!itemsCocina.isEmpty()) {
            PedidoCocinaEvent eventosCocina = new PedidoCocinaEvent(
                    pedido.getId(),
                    pedido.getMesa().getNombre(),
                    pedido.getNotas(),
                    itemsCocina
            );
            cocinaPublisher.publicar(eventosCocina);
            log.info("Evento enviado a Cocina — pedido: {}", pedidoId);
        }

        if (!itemsBar.isEmpty()) {
            PedidoBarEvent eventosBar = new PedidoBarEvent(
                    pedido.getId(),
                    pedido.getMesa().getNombre(),
                    pedido.getNotas(),
                    itemsBar
            );
            barPublisher.publicar(eventosBar);
            log.info("Evento enviado a Bar — pedido: {}", pedidoId);
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
                    "Solo se pueden entregar pedidos en estado LISTO_PARA_SERVIR. Estado actual: " + pedido.getEstado());
        }

        validarPropietarioOInstructor(pedido);

        pedido.setEstado(EstadoPedido.ENTREGADO);

        // Mesa pasa a POR_PAGAR
        Mesa mesa = pedido.getMesa();
        mesa.setEstado(EstadoMesa.POR_PAGAR);
        mesaRepository.save(mesa);

        log.info("Pedido {} marcado como ENTREGADO — mesa {} pasa a POR_PAGAR", pedidoId, mesa.getNombre());
        return pedidoMapper.toResponse(pedidoRepository.save(pedido));
    }

    // ─── CANCELAR ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PedidoResponse cancelar(UUID pedidoId) {

        Pedido pedido = obtenerPedidoOFalla(pedidoId);

        EstadoPedido estadoActual = pedido.getEstado();
        if (estadoActual == EstadoPedido.FACTURADO || estadoActual == EstadoPedido.CANCELADO) {
            throw new BusinessRuleException(
                    "No se puede cancelar un pedido en estado: " + estadoActual);
        }

        validarPropietarioOInstructor(pedido);

        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setFechaCierre(LocalDateTime.now());

        // Liberar la mesa si el pedido estaba activo
        if (estadoActual != EstadoPedido.BORRADOR) {
            Mesa mesa = pedido.getMesa();
            mesa.setEstado(EstadoMesa.LIBRE);
            mesaRepository.save(mesa);
            log.info("Mesa {} liberada por cancelación del pedido {}", mesa.getNombre(), pedidoId);
        }

        return pedidoMapper.toResponse(pedidoRepository.save(pedido));
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

        // Solo aceptamos transiciones válidas desde eventos externos
        boolean transicionValida = switch (nuevoEstado) {
            case EN_PREPARACION   -> estadoActual == EstadoPedido.ENVIADO_COCINA;
            case LISTO_PARA_SERVIR -> estadoActual == EstadoPedido.EN_PREPARACION;
            case CANCELADO        -> estadoActual != EstadoPedido.FACTURADO
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

    // ─── HELPERS PRIVADOS ──────────────────────────────────────────────────────

    private Pedido obtenerPedidoOFalla(UUID id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido no encontrado con id: " + id));
    }

    private BigDecimal calcularSubtotal(List<DetallePedido> detalles) {
        return detalles.stream()
                .map(DetallePedido::getSubtotalLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validarPropietarioOInstructor(Pedido pedido) {
        UUID currentUserId = UserContextHolder.getCurrentUserId();
        String currentRole  = UserContextHolder.getCurrentUserRole();

        boolean esPropietario = pedido.getMeseroId().equals(currentUserId);
        boolean esInstructor  = "INSTRUCTOR".equalsIgnoreCase(currentRole)
                || "ADMIN".equalsIgnoreCase(currentRole);

        if (!esPropietario && !esInstructor) {
            throw new BusinessRuleException(
                    "No tienes permiso para modificar este pedido");
        }
    }

    /**
     * Convierte DetallePedido (entidad guardada) a DetallePedidoRequest
     * para poder separar por categoría al confirmar.
     * NOTA: la categoría no se persiste en DetallePedido — se necesita
     * pasarla en el request original o añadir el campo al modelo.
     * Aquí asumimos que el campo 'observaciones' NO contiene la categoría
     * y que DetallePedido tendrá un campo 'categoria' adicional (ver nota abajo).
     */
    private DetallePedidoRequest detalleToRequest(DetallePedido d) {
        DetallePedidoRequest req = new DetallePedidoRequest();
        req.setProductoId(d.getProductoId());
        req.setNombreProducto(d.getNombreProducto());
        req.setCantidad(d.getCantidad());
        req.setPrecioUnitario(d.getPrecioUnitario());
        req.setCategoria(d.getCategoria()); // ver nota arquitectónica abajo
        req.setObservaciones(d.getObservaciones());
        return req;
    }
}

package co.edu.sena.ga_ms_restaurante.pedido.controller;

import co.edu.sena.ga_ms_restaurante.pedido.dto.request.PedidoCreateRequest;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResumenResponse;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResponse;
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.pedido.service.PedidoService;
import co.edu.sena.ga_ms_restaurante.security.UserContextHolder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    // POST /api/pedidos — crear pedido en BORRADOR
    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request));
    }

    // GET /api/pedidos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    // GET /api/pedidos — instructor/admin ve todos
    @GetMapping
    public ResponseEntity<List<PedidoResumenResponse>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    // GET /api/pedidos/mis-pedidos — mesero ve solo los suyos
    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoResumenResponse>> misPedidos() {
        UUID meseroId = UserContextHolder.getCurrentUserId();
        return ResponseEntity.ok(pedidoService.listarPorMesero(meseroId));
    }

    // GET /api/pedidos/estado/{estado}
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoResumenResponse>> listarPorEstado(
            @PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.listarPorEstado(estado));
    }

    // GET /api/pedidos/mesa/{mesaId}
    @GetMapping("/mesa/{mesaId}")
    public ResponseEntity<List<PedidoResumenResponse>> listarPorMesa(
            @PathVariable UUID mesaId) {
        return ResponseEntity.ok(pedidoService.listarPorMesa(mesaId));
    }

    // PATCH /api/pedidos/{id}/confirmar — BORRADOR → ENVIADO_COCINA + eventos RabbitMQ
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<PedidoResponse> confirmar(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoService.confirmarYEnviar(id));
    }

    // PATCH /api/pedidos/{id}/entregar — LISTO_PARA_SERVIR → ENTREGADO
    @PatchMapping("/{id}/entregar")
    public ResponseEntity<PedidoResponse> entregar(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoService.marcarEntregado(id));
    }

    // PATCH /api/pedidos/{id}/cancelar
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoService.cancelar(id));
    }
}

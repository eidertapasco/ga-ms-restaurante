package co.edu.sena.ga_ms_restaurante.pedido.controller;

import co.edu.sena.ga_ms_restaurante.pedido.dto.request.PedidoCreateRequest;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResumenResponse;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResponse;
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.pedido.service.PedidoService;
import co.edu.sena.ga_ms_restaurante.security.UserContextHolder;
import co.edu.sena.security.annotacion.RequireRole;
import co.edu.sena.security.enums.RolEnum;
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
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request));
    }

    // GET /api/pedidos/{id}
    @GetMapping("/{id}")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    // GET /api/pedidos — instructor/admin ve todos
    @GetMapping
    @RequireRole({RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<List<PedidoResumenResponse>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    // GET /api/pedidos/mis-pedidos — mesero ve solo los suyos
    @GetMapping("/mis-pedidos")
    @RequireRole({RolEnum.MESERO})
    public ResponseEntity<List<PedidoResumenResponse>> misPedidos() {
        UUID meseroId = UserContextHolder.getCurrentUserId();
        return ResponseEntity.ok(pedidoService.listarPorMesero(meseroId));
    }

    // GET /api/pedidos/estado/{estado}
    @GetMapping("/estado/{estado}")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<List<PedidoResumenResponse>> listarPorEstado(
            @PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.listarPorEstado(estado));
    }

    // GET /api/pedidos/mesa/{mesaId}
    @GetMapping("/mesa/{mesaId}")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<List<PedidoResumenResponse>> listarPorMesa(
            @PathVariable UUID mesaId) {
        return ResponseEntity.ok(pedidoService.listarPorMesa(mesaId));
    }

    // PATCH /api/pedidos/{id}/confirmar — BORRADOR → ENVIADO_COCINA + eventos RabbitMQ
    @PatchMapping("/{id}/confirmar")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<PedidoResponse> confirmar(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoService.confirmarYEnviar(id));
    }

    // PATCH /api/pedidos/{id}/entregar — LISTO_PARA_SERVIR → ENTREGADO
    @PatchMapping("/{id}/entregar")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<PedidoResponse> entregar(@PathVariable UUID id) {
        return ResponseEntity.ok(pedidoService.marcarEntregado(id));
    }

    // PATCH /api/pedidos/{id}/cancelar
    @PatchMapping("/{id}/cancelar")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<PedidoResponse> cancelar(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(pedidoService.cancelar(id, motivo));
    }

    // PATCH /api/pedidos/{id}/devolver?motivo=...
    @PatchMapping("/{id}/devolver")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<PedidoResponse> devolverGlobal(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(pedidoService.devolverGlobal(id, motivo));
    }

    // PATCH /api/pedidos/detalle/{idDetalle}/cancelar?motivo=...&cantidad=...
    // cantidad es opcional: si se omite, se cancela el ítem completo.
    @PatchMapping("/detalle/{idDetalle}/cancelar")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<PedidoResponse> cancelarDetalle(
            @PathVariable UUID idDetalle,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) Integer cantidad) {
        return ResponseEntity.ok(pedidoService.cancelarDetalle(idDetalle, motivo, cantidad));
    }

    // PATCH /api/pedidos/detalle/{idDetalle}/devolver?motivo=...&cantidad=...
    // cantidad es opcional: si se omite, se devuelve el ítem completo.
    @PatchMapping("/detalle/{idDetalle}/devolver")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<PedidoResponse> devolverDetalle(
            @PathVariable UUID idDetalle,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) Integer cantidad) {
        return ResponseEntity.ok(pedidoService.devolverDetalle(idDetalle, motivo, cantidad));
    }
}
package co.edu.sena.ga_ms_restaurante.controller;

import co.edu.sena.ga_ms_restaurante.dto.request.CrearPedidoRequestDTO;
import co.edu.sena.ga_ms_restaurante.dto.response.PedidoResponseDTO;
import co.edu.sena.ga_ms_restaurante.mapper.PedidoMapper;
import co.edu.sena.ga_ms_restaurante.model.DetallePedido;
import co.edu.sena.ga_ms_restaurante.model.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.model.Pedido;
import co.edu.sena.ga_ms_restaurante.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Tag(name = "Pedidos", description = "Gestión de pedidos del restaurante")
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoMapper pedidoMapper;

    public PedidoController(PedidoService pedidoService,
                            PedidoMapper pedidoMapper) {
        this.pedidoService = pedidoService;
        this.pedidoMapper = pedidoMapper;
    }

    /* =========================
       CREAR PEDIDO
       ========================= */

    @Operation(
            summary = "Crear un nuevo pedido",
            description = "Registra un pedido con sus productos y calcula el total automáticamente"
    )
    @ApiResponse(responseCode = "201", description = "Pedido creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(
            @RequestBody @Valid CrearPedidoRequestDTO request) {

        // 1️⃣ Request → Entity (parcial)
        Pedido pedido = pedidoMapper.toEntity(request);

        // 2️⃣ Crear detalles base desde DTO
        List<DetallePedido> detalles = request.getDetalles().stream()
                .map(pedidoMapper::toDetalleEntity)
                .collect(Collectors.toList());

        detalles.forEach(pedido::agregarDetalle);

        // 3️⃣ Lógica de negocio
        Pedido pedidoCreado = pedidoService.crearPedido(pedido);

        // 4️⃣ Entity → Response
        PedidoResponseDTO response = pedidoMapper.toResponse(pedidoCreado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /* =========================
       OBTENER PEDIDO POR ID
       ========================= */

    @Operation(summary = "Obtener pedido por ID")
    @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtenerPedido(@PathVariable Long id) {

        Pedido pedido = pedidoService.obtenerPedidoPorId(id);
        return ResponseEntity.ok(pedidoMapper.toResponse(pedido));
    }

    /* =========================
       LISTAR PEDIDOS
       ========================= */

    @Operation(summary = "Listar todos los pedidos")
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarPedidos() {

        List<PedidoResponseDTO> response = pedidoService.listarPedidos().stream()
                .map(pedidoMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /* =========================
       CAMBIAR ESTADO
       ========================= */

    @Operation(summary = "Cambiar estado de un pedido")
    @ApiResponse(responseCode = "400", description = "Cambio de estado inválido")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPedido estado) {

        Pedido pedido = pedidoService.cambiarEstado(id, estado);
        return ResponseEntity.ok(pedidoMapper.toResponse(pedido));
    }

    /* =========================
       CANCELAR PEDIDO
       ========================= */

    @Operation(
            summary = "Cancelar un pedido",
            description = "Cancela un pedido siempre que no esté en estado ENTREGADO"
    )
    @ApiResponse(
            responseCode = "200", description = "Pedido cancelado correctamente"
    )
    @ApiResponse(
            responseCode = "400", description = "No se puede cancelar un pedido entregado"
    )
    @ApiResponse(
            responseCode = "404", description = "Pedido no encontrado"
    )
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelarPedido(@PathVariable Long id) {

        Pedido pedido = pedidoService.cancelarPedido(id);
        return ResponseEntity.ok(pedidoMapper.toResponse(pedido));
    }
}

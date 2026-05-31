package co.edu.sena.ga_ms_restaurante.mesa.controller;

import co.edu.sena.ga_ms_restaurante.mesa.dto.request.MesaCreateRequest;
import co.edu.sena.ga_ms_restaurante.mesa.dto.request.MesaUpdateRequest;
import co.edu.sena.ga_ms_restaurante.mesa.dto.response.MesaResponse;
import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.mesa.service.MesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
public class MesaController {

    private final MesaService mesaService;

    // ─── Endpoints de consulta (mesero, cajero, instructor) ──────────────────

    @GetMapping
    public ResponseEntity<List<MesaResponse>> listarActivas() {
        return ResponseEntity.ok(mesaService.listarMesasActivas());
    }

    @GetMapping("/inactivas")
    public ResponseEntity<List<MesaResponse>> listarInactivas() {
        return ResponseEntity.ok(mesaService.listarMesasInactivas());
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<MesaResponse>> listarPorEstado(
            @PathVariable EstadoMesa estado) {
        return ResponseEntity.ok(mesaService.listarMesasPorEstado(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(mesaService.buscarPorId(id));
    }

    // ─── Endpoints de gestión (solo instructor) ───────────────────────────────

    @PostMapping
    public ResponseEntity<MesaResponse> crear(
            @RequestBody @Valid MesaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mesaService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MesaResponse> actualizar(
            @PathVariable UUID id,
            @RequestBody @Valid MesaUpdateRequest request) {
        return ResponseEntity.ok(mesaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<MesaResponse> cambiarEstado(
            @PathVariable UUID id,
            @RequestParam EstadoMesa nuevoEstado) {
        return ResponseEntity.ok(mesaService.cambiarEstado(id, nuevoEstado));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<MesaResponse> activar(@PathVariable UUID id) {
        return ResponseEntity.ok(mesaService.activar(id));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<MesaResponse> desactivar(@PathVariable UUID id) {
        return ResponseEntity.ok(mesaService.desactivar(id));
    }
}
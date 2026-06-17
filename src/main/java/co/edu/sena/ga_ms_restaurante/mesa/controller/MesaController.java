package co.edu.sena.ga_ms_restaurante.mesa.controller;

import co.edu.sena.ga_ms_restaurante.mesa.dto.request.MesaCreateRequest;
import co.edu.sena.ga_ms_restaurante.mesa.dto.request.MesaUpdateRequest;
import co.edu.sena.ga_ms_restaurante.mesa.dto.response.MesaResponse;
import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.mesa.service.MesaService;
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
@RequestMapping("/api/mesas")
@RequiredArgsConstructor
public class MesaController {

    private final MesaService mesaService;

    @GetMapping
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<List<MesaResponse>> listarActivas() {
        return ResponseEntity.ok(mesaService.listarMesasActivas());
    }

    @GetMapping("/inactivas")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<List<MesaResponse>> listarInactivas() {
        return ResponseEntity.ok(mesaService.listarMesasInactivas());
    }

    @GetMapping("/estado/{estado}")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<List<MesaResponse>> listarPorEstado(@PathVariable EstadoMesa estado) {
        return ResponseEntity.ok(mesaService.listarMesasPorEstado(estado));
    }

    @GetMapping("/{id}")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<MesaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(mesaService.buscarPorId(id));
    }

    @PostMapping
    @RequireRole({RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<MesaResponse> crear(@RequestBody @Valid MesaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mesaService.crear(request));
    }

    @PutMapping("/{id}")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<MesaResponse> actualizar(@PathVariable UUID id, @RequestBody @Valid MesaUpdateRequest request) {
        return ResponseEntity.ok(mesaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @RequireRole({RolEnum.MESERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<MesaResponse> cambiarEstado(@PathVariable UUID id, @RequestParam EstadoMesa nuevoEstado) {
        return ResponseEntity.ok(mesaService.cambiarEstado(id, nuevoEstado));
    }

    @PatchMapping("/{id}/activar")
    @RequireRole({RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<MesaResponse> activar(@PathVariable UUID id) {
        return ResponseEntity.ok(mesaService.activar(id));
    }

    @PatchMapping("/{id}/desactivar")
    @RequireRole({RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<MesaResponse> desactivar(@PathVariable UUID id) {
        return ResponseEntity.ok(mesaService.desactivar(id));
    }
}
package co.edu.sena.ga_ms_restaurante.caja.controller;

import co.edu.sena.ga_ms_restaurante.caja.dto.request.AbrirSesionRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.request.CerrarSesionRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.SesionCajaResponse;
import co.edu.sena.ga_ms_restaurante.caja.service.CajaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    @PostMapping("/sesion/abrir")
    public ResponseEntity<SesionCajaResponse> abrirSesion(
            @Valid @RequestBody AbrirSesionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.abrirSesion(request));
    }

    @PatchMapping("/sesion/{id}/cerrar")
    public ResponseEntity<SesionCajaResponse> cerrarSesion(
            @PathVariable UUID id,
            @Valid @RequestBody CerrarSesionRequest request) {
        return ResponseEntity.ok(cajaService.cerrarSesion(id, request));
    }

    @GetMapping("/sesion/activa")
    public ResponseEntity<SesionCajaResponse> sesionActiva() {
        return ResponseEntity.ok(cajaService.sesionActiva());
    }

    @GetMapping("/sesion/{id}")
    public ResponseEntity<SesionCajaResponse> buscarSesion(@PathVariable UUID id) {
        return ResponseEntity.ok(cajaService.buscarSesionPorId(id));
    }
}
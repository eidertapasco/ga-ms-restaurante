package co.edu.sena.ga_ms_restaurante.caja.controller;

import co.edu.sena.ga_ms_restaurante.caja.dto.request.AbrirSesionRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.request.CerrarSesionRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.request.FacturarPedidoRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.FacturaResponse;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.SesionCajaResponse;
import co.edu.sena.ga_ms_restaurante.caja.service.CajaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    // ── Sesión ────────────────────────────────────────────────────────────────

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

    // ── Facturas ──────────────────────────────────────────────────────────────

    @PostMapping("/facturar")
    public ResponseEntity<FacturaResponse> facturar(
            @Valid @RequestBody FacturarPedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.facturar(request));
    }

    @PatchMapping("/facturas/{id}/anular")
    public ResponseEntity<FacturaResponse> anular(@PathVariable UUID id) {
        return ResponseEntity.ok(cajaService.anularFactura(id));
    }

    @GetMapping("/facturas/{id}")
    public ResponseEntity<FacturaResponse> buscarFacturaPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(cajaService.buscarFacturaPorId(id));
    }

    @GetMapping("/facturas/numero/{numero}")
    public ResponseEntity<FacturaResponse> buscarPorNumero(@PathVariable String numero) {
        return ResponseEntity.ok(cajaService.buscarFacturaPorNumero(numero));
    }

    @GetMapping("/sesion/{id}/facturas")
    public ResponseEntity<List<FacturaResponse>> facturasDeSesion(@PathVariable UUID id) {
        return ResponseEntity.ok(cajaService.listarFacturasDeSesion(id));
    }
}

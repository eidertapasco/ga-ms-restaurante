package co.edu.sena.ga_ms_restaurante.caja.controller;

import co.edu.sena.ga_ms_restaurante.caja.dto.request.FacturarPedidoRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.FacturaResponse;
import co.edu.sena.ga_ms_restaurante.caja.service.FacturaService;
import co.edu.sena.security.annotacion.RequireRole;
import co.edu.sena.security.enums.RolEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @PostMapping
    @RequireRole({RolEnum.CAJERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<FacturaResponse> facturar(@Valid @RequestBody FacturarPedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.facturar(request));
    }

    @PatchMapping("/{id}/anular")
    @RequireRole({RolEnum.CAJERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<FacturaResponse> anular(@PathVariable UUID id) {
        return ResponseEntity.ok(facturaService.anularFactura(id));
    }

    @GetMapping("/{id}")
    @RequireRole({RolEnum.CAJERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<FacturaResponse> buscarFacturaPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(facturaService.buscarFacturaPorId(id));
    }

    @GetMapping("/numero/{numero}")
    @RequireRole({RolEnum.CAJERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<FacturaResponse> buscarPorNumero(@PathVariable String numero) {
        return ResponseEntity.ok(facturaService.buscarFacturaPorNumero(numero));
    }

    @GetMapping("/sesion/{sesionId}")
    @RequireRole({RolEnum.CAJERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<List<FacturaResponse>> facturasDeSesion(@PathVariable UUID sesionId) {
        return ResponseEntity.ok(facturaService.listarFacturasDeSesion(sesionId));
    }

    @GetMapping("/{id}/pdf")
    @RequireRole({RolEnum.CAJERO, RolEnum.INSTRUCTOR, RolEnum.ADMINISTRADOR})
    public ResponseEntity<byte[]> descargarFacturaPdf(@PathVariable UUID id) {
        byte[] pdfBytes = facturaService.generarFacturaPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Factura-" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
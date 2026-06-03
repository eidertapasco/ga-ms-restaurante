package co.edu.sena.ga_ms_restaurante.caja.controller;

import co.edu.sena.ga_ms_restaurante.caja.dto.request.FacturarPedidoRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.FacturaResponse;
import co.edu.sena.ga_ms_restaurante.caja.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// AÑADIDO: Importaciones para el manejo de archivos HTTP y PDF
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @PostMapping
    public ResponseEntity<FacturaResponse> facturar(
            @Valid @RequestBody FacturarPedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.facturar(request));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<FacturaResponse> anular(@PathVariable UUID id) {
        return ResponseEntity.ok(facturaService.anularFactura(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponse> buscarFacturaPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(facturaService.buscarFacturaPorId(id));
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<FacturaResponse> buscarPorNumero(@PathVariable String numero) {
        return ResponseEntity.ok(facturaService.buscarFacturaPorNumero(numero));
    }

    @GetMapping("/sesion/{sesionId}")
    public ResponseEntity<List<FacturaResponse>> facturasDeSesion(@PathVariable UUID sesionId) {
        return ResponseEntity.ok(facturaService.listarFacturasDeSesion(sesionId));
    }

    // AÑADIDO: Endpoint para descargar la factura en formato PDF
    @GetMapping("/{id}/pdf")
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
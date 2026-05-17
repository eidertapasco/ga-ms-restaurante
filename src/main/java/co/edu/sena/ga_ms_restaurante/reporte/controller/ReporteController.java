package co.edu.sena.ga_ms_restaurante.reporte.controller;

import co.edu.sena.ga_ms_restaurante.reporte.dto.response.PedidoPorMesaResponse;
import co.edu.sena.ga_ms_restaurante.reporte.dto.response.ResumenVentasResponse;
import co.edu.sena.ga_ms_restaurante.reporte.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/sesion/{sesionId}/resumen")
    public ResponseEntity<ResumenVentasResponse> resumenPorSesion(
            @PathVariable UUID sesionId) {
        return ResponseEntity.ok(reporteService.resumenVentasPorSesion(sesionId));
    }

    @GetMapping("/mesas")
    public ResponseEntity<List<PedidoPorMesaResponse>> pedidosPorMesa() {
        return ResponseEntity.ok(reporteService.pedidosPorMesa());
    }
}
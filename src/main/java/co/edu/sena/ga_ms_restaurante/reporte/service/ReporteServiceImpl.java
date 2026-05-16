package co.edu.sena.ga_ms_restaurante.reporte.service;

import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoFactura;
import co.edu.sena.ga_ms_restaurante.caja.enums.MetodoPago;
import co.edu.sena.ga_ms_restaurante.caja.model.Factura;
import co.edu.sena.ga_ms_restaurante.caja.repository.FacturaRepository;
import co.edu.sena.ga_ms_restaurante.caja.repository.SesionCajaRepository;
import co.edu.sena.ga_ms_restaurante.exception.custom.ResourceNotFoundException;
import co.edu.sena.ga_ms_restaurante.mesa.repository.MesaRepository;
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.pedido.repository.PedidoRepository;
import co.edu.sena.ga_ms_restaurante.reporte.dto.response.PedidoPorMesaResponse;
import co.edu.sena.ga_ms_restaurante.reporte.dto.response.ResumenVentasResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final FacturaRepository facturaRepository;
    private final SesionCajaRepository sesionCajaRepository;
    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;

    @Override
    @Transactional(readOnly = true)
    public ResumenVentasResponse resumenVentasPorSesion(UUID sesionId) {

        sesionCajaRepository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sesión no encontrada con id: " + sesionId));

        List<Factura> facturas = facturaRepository.findBySesionCaja_Id(sesionId);

        List<Factura> pagadas = facturas.stream()
                .filter(f -> f.getEstado() == EstadoFactura.PAGADA)
                .toList();

        long cancelados = pedidoRepository.findByEstado(EstadoPedido.CANCELADO).size();

        BigDecimal totalBruto    = sumar(pagadas.stream().map(Factura::getSubtotal).toList());
        BigDecimal totalPropinas = sumar(pagadas.stream().map(Factura::getPropina).toList());
        BigDecimal totalConPropina = totalBruto.add(totalPropinas);

        BigDecimal efectivo      = sumarPorMetodo(pagadas, MetodoPago.EFECTIVO);
        BigDecimal tarjeta       = sumarPorMetodo(pagadas, MetodoPago.TARJETA);
        BigDecimal transferencia = sumarPorMetodo(pagadas, MetodoPago.TRANSFERENCIA);
        BigDecimal cortesia      = sumarPorMetodo(pagadas, MetodoPago.CORTESIA);

        return new ResumenVentasResponse(
                pagadas.size(),
                cancelados,
                totalBruto,
                totalPropinas,
                totalConPropina,
                efectivo,
                tarjeta,
                transferencia,
                cortesia
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoPorMesaResponse> pedidosPorMesa() {

        return mesaRepository.findByActivoTrue().stream()
                .map(mesa -> {
                    List<Factura> facturasMesa = facturaRepository
                            .findBySesionCaja_Id(mesa.getId()) // no aplica — ver nota
                            .stream().toList();

                    // Conteo de pedidos facturados por mesa
                    long totalPedidos = pedidoRepository
                            .findByMesa_Id(mesa.getId())
                            .stream()
                            .filter(p -> p.getEstado() == EstadoPedido.FACTURADO)
                            .count();

                    BigDecimal totalFacturado = pedidoRepository
                            .findByMesa_Id(mesa.getId())
                            .stream()
                            .filter(p -> p.getEstado() == EstadoPedido.FACTURADO)
                            .map(p -> facturaRepository.findByPedido_Id(p.getId())
                                    .map(Factura::getTotal)
                                    .orElse(BigDecimal.ZERO))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new PedidoPorMesaResponse(
                            mesa.getId(),
                            mesa.getNombre(),
                            totalPedidos,
                            totalFacturado
                    );
                }).toList();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private BigDecimal sumar(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumarPorMetodo(List<Factura> facturas, MetodoPago metodo) {
        return facturas.stream()
                .filter(f -> f.getMetodoPago() == metodo)
                .map(Factura::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

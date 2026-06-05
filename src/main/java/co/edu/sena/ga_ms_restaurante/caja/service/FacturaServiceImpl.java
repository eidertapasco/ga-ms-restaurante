package co.edu.sena.ga_ms_restaurante.caja.service;

import co.edu.sena.ga_ms_restaurante.caja.dto.request.FacturarPedidoRequest;
import co.edu.sena.ga_ms_restaurante.caja.dto.response.FacturaResponse;
import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoFactura;
import co.edu.sena.ga_ms_restaurante.caja.enums.EstadoSesion;
import co.edu.sena.ga_ms_restaurante.caja.enums.MetodoPago;
import co.edu.sena.ga_ms_restaurante.caja.mapper.FacturaMapper;
import co.edu.sena.ga_ms_restaurante.caja.model.Factura;
import co.edu.sena.ga_ms_restaurante.caja.model.SesionCaja;
import co.edu.sena.ga_ms_restaurante.caja.repository.FacturaRepository;
import co.edu.sena.ga_ms_restaurante.caja.repository.SesionCajaRepository;
import co.edu.sena.ga_ms_restaurante.exception.custom.BusinessRuleException;
import co.edu.sena.ga_ms_restaurante.exception.custom.ResourceNotFoundException;
import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.mesa.model.Mesa;
import co.edu.sena.ga_ms_restaurante.mesa.repository.MesaRepository;
import co.edu.sena.ga_ms_restaurante.pedido.enums.EstadoPedido;
import co.edu.sena.ga_ms_restaurante.pedido.model.DetallePedido; // AÑADIDO
import co.edu.sena.ga_ms_restaurante.pedido.model.Pedido;
import co.edu.sena.ga_ms_restaurante.pedido.repository.PedidoRepository;
import co.edu.sena.ga_ms_restaurante.security.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importaciones para la generación de PDF y estilos (NUEVAS)
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacturaServiceImpl implements FacturaService {

    private final SesionCajaRepository sesionCajaRepository;
    private final FacturaRepository facturaRepository;
    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final FacturaMapper facturaMapper;

    @Override
    @Transactional
    public FacturaResponse facturar(FacturarPedidoRequest request) {
        SesionCaja sesion = sesionCajaRepository.findByEstado(EstadoSesion.ABIERTA)
                .orElseThrow(() -> new BusinessRuleException(
                        "No hay sesión de caja abierta. Abre una sesión antes de facturar."));

        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido no encontrado con id: " + request.getPedidoId()));

        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
            throw new BusinessRuleException(
                    "Solo se pueden facturar pedidos en estado ENTREGADO. Estado actual: " + pedido.getEstado());
        }

        facturaRepository.findByPedido_Id(pedido.getId()).ifPresent(f -> {
            throw new BusinessRuleException(
                    "Este pedido ya fue facturado. Número: " + f.getNumeroFactura());
        });

        BigDecimal subtotal = pedido.getSubtotal();
        BigDecimal propina  = request.getPropina();
        BigDecimal total    = subtotal.add(propina);

        Factura factura = new Factura();
        factura.setNumeroFactura(generarNumeroFactura());
        factura.setPedido(pedido);
        factura.setSesionCaja(sesion);
        factura.setCajeroId(UserContextHolder.getCurrentUserId());
        factura.setSubtotal(subtotal);
        factura.setPropina(propina);
        factura.setTotal(total);
        factura.setMetodoPago(request.getMetodoPago());
        factura.setEstado(EstadoFactura.PAGADA);

        acumularEnSesion(sesion, request.getMetodoPago(), total);
        sesionCajaRepository.save(sesion);

        pedido.setEstado(EstadoPedido.FACTURADO);
        pedido.setFechaCierre(LocalDateTime.now());
        pedidoRepository.save(pedido);

        Mesa mesa = pedido.getMesa();
        mesa.setEstado(EstadoMesa.LIBRE);
        mesaRepository.save(mesa);

        Factura guardada = facturaRepository.save(factura);
        log.info("Factura {} emitida — pedido: {}, total: {}", guardada.getNumeroFactura(), pedido.getId(), total);

        return facturaMapper.toFacturaResponse(guardada);
    }

    @Override
    @Transactional
    public FacturaResponse anularFactura(UUID facturaId) {
        Factura factura = obtenerFacturaOFalla(facturaId);

        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new BusinessRuleException("La factura ya está anulada.");
        }

        SesionCaja sesion = factura.getSesionCaja();
        if (sesion.getEstado() == EstadoSesion.ABIERTA) {
            revertirEnSesion(sesion, factura.getMetodoPago(), factura.getTotal());
            sesionCajaRepository.save(sesion);
        }

        Pedido pedido = factura.getPedido();
        pedido.setEstado(EstadoPedido.ENTREGADO);
        pedido.setFechaCierre(null);
        pedidoRepository.save(pedido);

        Mesa mesa = pedido.getMesa();
        mesa.setEstado(EstadoMesa.POR_PAGAR);
        mesaRepository.save(mesa);

        factura.setEstado(EstadoFactura.ANULADA);
        log.info("Factura {} anulada", factura.getNumeroFactura());

        return facturaMapper.toFacturaResponse(facturaRepository.save(factura));
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponse buscarFacturaPorId(UUID facturaId) {
        return facturaMapper.toFacturaResponse(obtenerFacturaOFalla(facturaId));
    }

    @Override
    @Transactional(readOnly = true)
    public FacturaResponse buscarFacturaPorNumero(String numeroFactura) {
        Factura factura = facturaRepository.findByNumeroFactura(numeroFactura)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Factura no encontrada con número: " + numeroFactura));
        return facturaMapper.toFacturaResponse(factura);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponse> listarFacturasDeSesion(UUID sesionId) {
        sesionCajaRepository.findById(sesionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de caja no encontrada con id: " + sesionId));
        return facturaMapper.toFacturaResponseList(facturaRepository.findBySesionCaja_Id(sesionId));
    }

    // MÉTODO PDF REDISEÑADO CON FORMATO DE TIRILLA E ITEMS
    @Override
    @Transactional(readOnly = true)
    public byte[] generarFacturaPdf(UUID facturaId) {
        Factura factura = obtenerFacturaOFalla(facturaId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Configurar los tipos de letra (Fuentes)
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            // 2. Encabezado centrado
            Paragraph titulo = new Paragraph("GastroSena\n¡Sabor y Tradición!\n", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            // Formatear la fecha para que se vea amigable (ej: 03/06/26 16:03:52)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
            String fechaFormateada = factura.getFechaEmision() != null ?
                    factura.getFechaEmision().format(formatter) :
                    LocalDateTime.now().format(formatter);

            // 3. Información general del recibo
            document.add(new Paragraph("NIT-123456789-0", fontBold)); // NIT ficticio
            document.add(new Paragraph("Mesa: " + factura.getPedido().getMesa().getNombre(), fontNormal));
            document.add(new Paragraph("Personas: " + factura.getPedido().getNumeroComensales(), fontNormal));
            document.add(new Paragraph("ID Factura: " + factura.getNumeroFactura(), fontNormal));
            document.add(new Paragraph("Fecha: " + fechaFormateada, fontNormal));

            // Como el mesero es un UUID, mostramos solo los primeros 8 caracteres para que no ocupe tanto espacio
            String cajeroIdCorto = factura.getCajeroId().toString().substring(0, 8);
            document.add(new Paragraph("Cajero: #" + cajeroIdCorto, fontNormal));
            document.add(new Paragraph("Método de Pago: " + factura.getMetodoPago(), fontNormal));

            document.add(new Paragraph("\n"));

            // 4. Crear la tabla para los items (Cantidad | Nombre | Precio)
            PdfPTable tablaItems = new PdfPTable(3);
            tablaItems.setWidthPercentage(100);
            // Proporciones: La columna del nombre es la más ancha
            tablaItems.setWidths(new float[]{1f, 5f, 2f});

            // Recorrer los detalles (items) del pedido
            for (DetallePedido detalle : factura.getPedido().getDetalles()) {
                // Cantidad
                PdfPCell cellCant = new PdfPCell(new Paragraph(String.valueOf(detalle.getCantidad()), fontNormal));
                cellCant.setBorder(PdfPCell.NO_BORDER);

                // Nombre del Producto
                PdfPCell cellNombre = new PdfPCell(new Paragraph(detalle.getNombreProducto(), fontNormal));
                cellNombre.setBorder(PdfPCell.NO_BORDER);

                // Precio (Alineado a la derecha)
                PdfPCell cellPrecio = new PdfPCell(new Paragraph("$" + detalle.getSubtotalLinea(), fontNormal));
                cellPrecio.setBorder(PdfPCell.NO_BORDER);
                cellPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);

                tablaItems.addCell(cellCant);
                tablaItems.addCell(cellNombre);
                tablaItems.addCell(cellPrecio);
            }
            // Añadir la tabla de items al documento
            document.add(tablaItems);

            document.add(new Paragraph("-------------------------------------------------------------------"));

            // 5. Crear tabla para Totales (Subtotal y Total alineados a la derecha)
            PdfPTable tablaTotales = new PdfPTable(2);
            tablaTotales.setWidthPercentage(100);
            tablaTotales.setWidths(new float[]{3f, 1f});

            // Subtotal
            PdfPCell cellSubLabel = new PdfPCell(new Paragraph("Subtotal", fontNormal));
            cellSubLabel.setBorder(PdfPCell.NO_BORDER);
            PdfPCell cellSubValor = new PdfPCell(new Paragraph("$" + factura.getSubtotal(), fontNormal));
            cellSubValor.setBorder(PdfPCell.NO_BORDER);
            cellSubValor.setHorizontalAlignment(Element.ALIGN_RIGHT);

            tablaTotales.addCell(cellSubLabel);
            tablaTotales.addCell(cellSubValor);

            // Total Final
            PdfPCell cellTotLabel = new PdfPCell(new Paragraph("Total", fontBold));
            cellTotLabel.setBorder(PdfPCell.NO_BORDER);
            PdfPCell cellTotValor = new PdfPCell(new Paragraph("$" + factura.getTotal(), fontBold));
            cellTotValor.setBorder(PdfPCell.NO_BORDER);
            cellTotValor.setHorizontalAlignment(Element.ALIGN_RIGHT);

            tablaTotales.addCell(cellTotLabel);
            tablaTotales.addCell(cellTotValor);

            document.add(tablaTotales);

            document.add(new Paragraph("\n"));

            // 6. Pie de página centrado
            Paragraph footer = new Paragraph("¡Gracias por su visita!", fontBold);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new BusinessRuleException("Error al generar el PDF de la factura: " + e.getMessage());
        }
    }

    private Factura obtenerFacturaOFalla(UUID id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada con id: " + id));
    }

    private String generarNumeroFactura() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sufijo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "FAC-" + fecha + "-" + sufijo;
    }

    private void acumularEnSesion(SesionCaja sesion, MetodoPago metodo, BigDecimal total) {
        switch (metodo) {
            case EFECTIVO    -> sesion.setTotalVentasEfectivo(sesion.getTotalVentasEfectivo().add(total));
            case TARJETA     -> sesion.setTotalVentasTarjeta(sesion.getTotalVentasTarjeta().add(total));
            case TRANSFERENCIA -> sesion.setTotalVentasTransferencia(sesion.getTotalVentasTransferencia().add(total));
            case CORTESIA    -> { }
        }
    }

    private void revertirEnSesion(SesionCaja sesion, MetodoPago metodo, BigDecimal total) {
        switch (metodo) {
            case EFECTIVO    -> sesion.setTotalVentasEfectivo(sesion.getTotalVentasEfectivo().subtract(total));
            case TARJETA     -> sesion.setTotalVentasTarjeta(sesion.getTotalVentasTarjeta().subtract(total));
            case TRANSFERENCIA -> sesion.setTotalVentasTransferencia(sesion.getTotalVentasTransferencia().subtract(total));
            case CORTESIA    -> { }
        }
    }
}
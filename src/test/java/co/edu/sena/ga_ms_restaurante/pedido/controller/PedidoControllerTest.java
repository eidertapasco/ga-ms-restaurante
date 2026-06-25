package co.edu.sena.ga_ms_restaurante.pedido.controller;

import co.edu.sena.ga_ms_restaurante.exception.custom.BusinessRuleException;
import co.edu.sena.ga_ms_restaurante.exception.custom.ResourceNotFoundException;
import co.edu.sena.ga_ms_restaurante.pedido.dto.request.DetallePedidoRequest;
import co.edu.sena.ga_ms_restaurante.pedido.dto.request.PedidoCreateRequest;
import co.edu.sena.ga_ms_restaurante.pedido.dto.response.PedidoResponse;
import co.edu.sena.ga_ms_restaurante.pedido.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PedidoService pedidoService;

    // Helper para crear un detalle válido y pasar la validación @Valid del DTO
    private DetallePedidoRequest crearDetalleValido() {
        DetallePedidoRequest detalle = new DetallePedidoRequest();
        detalle.setProductoId("PROD-01");
        detalle.setNombreProducto("Bandeja Paisa");
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(new BigDecimal("25000.00"));
        detalle.setCategoria("COMIDA");
        return detalle;
    }

    // ==========================================
    // CP09: Abrir/Crear pedido en una mesa
    // ==========================================

    @Test
    @DisplayName("CP09.1: Apertura exitosa de pedido en mesa libre")
    void testCrearPedidoExitoso() throws Exception {
        PedidoCreateRequest requestDTO = new PedidoCreateRequest();
        requestDTO.setMesaId(UUID.randomUUID());
        requestDTO.setNumeroComensales(4);
        requestDTO.setDetalles(List.of(crearDetalleValido())); // <-- ¡Aquí le mandamos el ítem válido!

        Mockito.when(pedidoService.crear(any(PedidoCreateRequest.class)))
                .thenReturn(new PedidoResponse());

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("CP09.2: Bloqueo al abrir pedido en mesa ocupada")
    void testCrearPedidoMesaOcupada() throws Exception {
        PedidoCreateRequest requestDTO = new PedidoCreateRequest();
        requestDTO.setMesaId(UUID.randomUUID());
        requestDTO.setNumeroComensales(4);
        requestDTO.setDetalles(List.of(crearDetalleValido())); // <-- ¡Aquí también!

        // Simulamos que el backend bloquea por regla de negocio
        Mockito.when(pedidoService.crear(any(PedidoCreateRequest.class)))
                .thenThrow(new BusinessRuleException("La mesa no está disponible"));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnprocessableEntity());
    }

    // ==========================================
    // CP15: Consultar detalle de un pedido activo
    // ==========================================

    @Test
    @DisplayName("CP15.1: Consultar detalle de pedido exitosamente")
    void testConsultarPedidoPorId() throws Exception {
        UUID idPedido = UUID.randomUUID();

        Mockito.when(pedidoService.buscarPorId(eq(idPedido)))
                .thenReturn(new PedidoResponse());

        mockMvc.perform(get("/api/pedidos/" + idPedido))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CP15.2: Consultar pedido que no existe")
    void testConsultarPedidoNoEncontrado() throws Exception {
        UUID idPedido = UUID.randomUUID();

        // Simulamos el error 404 si el pedido no existe
        Mockito.when(pedidoService.buscarPorId(eq(idPedido)))
                .thenThrow(new ResourceNotFoundException("El pedido no existe"));

        mockMvc.perform(get("/api/pedidos/" + idPedido))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // CP20: Actualizar estado del pedido (Confirmar)
    // ==========================================

    @Test
    @DisplayName("CP20.1: Confirmación exitosa del pedido (Enviar a cocina)")
    void testConfirmarPedidoExitoso() throws Exception {
        UUID idPedido = UUID.randomUUID();

        Mockito.when(pedidoService.confirmarYEnviar(eq(idPedido)))
                .thenReturn(new PedidoResponse());

        mockMvc.perform(patch("/api/pedidos/" + idPedido + "/confirmar"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CP20.2: Bloqueo por estado no válido al confirmar")
    void testConfirmarPedidoInvalido() throws Exception {
        UUID idPedido = UUID.randomUUID();

        Mockito.when(pedidoService.confirmarYEnviar(eq(idPedido)))
                .thenThrow(new BusinessRuleException("Solo se pueden confirmar pedidos en estado BORRADOR"));

        mockMvc.perform(patch("/api/pedidos/" + idPedido + "/confirmar"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ==========================================
    // CP21: Finalizar entrega del pedido (La única con 3 CPs)
    // ==========================================

    @Test
    @DisplayName("CP21.1: Finalización exitosa de la entrega del pedido")
    void testEntregarPedidoExitoso() throws Exception {
        UUID idPedido = UUID.randomUUID();

        Mockito.when(pedidoService.marcarEntregado(eq(idPedido)))
                .thenReturn(new PedidoResponse());

        mockMvc.perform(patch("/api/pedidos/" + idPedido + "/entregar"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CP21.2: Bloqueo por estado no válido y fallos técnicos")
    void testEntregarPedidoError() throws Exception {
        UUID idPedido = UUID.randomUUID();

        Mockito.when(pedidoService.marcarEntregado(eq(idPedido)))
                .thenThrow(new RuntimeException("Error de conexión al intentar finalizar la entrega"));

        mockMvc.perform(patch("/api/pedidos/" + idPedido + "/entregar"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("CP21.3: Cancelación del pedido con justificación")
    void testCancelarPedidoExitosa() throws Exception {
        UUID idPedido = UUID.randomUUID();
        String motivo = "El cliente se retiró del local";

        Mockito.when(pedidoService.cancelar(eq(idPedido), eq(motivo)))
                .thenReturn(new PedidoResponse());

        mockMvc.perform(patch("/api/pedidos/" + idPedido + "/cancelar")
                        .param("motivo", motivo))
                .andExpect(status().isOk());
    }
}
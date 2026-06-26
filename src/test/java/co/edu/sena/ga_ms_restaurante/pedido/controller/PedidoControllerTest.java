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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    // Helper para crear un detalle válido
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
    // HU12: Gestionar carrito de pedidos
    // ==========================================

    @Test
    @DisplayName("CP12.1 - Gestión completa y confirmación del carrito de pedidos")
    void testConfirmarCarritoExitoso() throws Exception {
        PedidoCreateRequest requestDTO = new PedidoCreateRequest();
        requestDTO.setMesaId(UUID.randomUUID());
        requestDTO.setNumeroComensales(4);
        requestDTO.setDetalles(List.of(crearDetalleValido())); // Carrito con productos

        Mockito.when(pedidoService.crear(any(PedidoCreateRequest.class)))
                .thenReturn(new PedidoResponse());

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated()); // Espera un 201 Created
    }

    @Test
    @DisplayName("CP12.2 - Vaciado de carrito y validación de estado sin productos")
    void testValidacionCarritoVacio() throws Exception {
        PedidoCreateRequest requestDTO = new PedidoCreateRequest();
        requestDTO.setMesaId(UUID.randomUUID());
        requestDTO.setNumeroComensales(4);
        requestDTO.setDetalles(Collections.emptyList()); // Carrito vacío sin productos

        // Como el @NotEmpty valida que haya productos, no llega al servicio y lanza un Error 400 Bad Request
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    // ==========================================
    // HU17: Consultar estado del pedido
    // ==========================================

    @Test
    @DisplayName("CP17.1: Consultar estado de pedido exitosamente")
    void testConsultarPedidoPorId() throws Exception {
        UUID idPedido = UUID.randomUUID();

        Mockito.when(pedidoService.buscarPorId(eq(idPedido)))
                .thenReturn(new PedidoResponse());

        mockMvc.perform(get("/api/pedidos/" + idPedido))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CP17.2: Consultar estado de pedido que no existe")
    void testConsultarPedidoNoEncontrado() throws Exception {
        UUID idPedido = UUID.randomUUID();

        Mockito.when(pedidoService.buscarPorId(eq(idPedido)))
                .thenThrow(new ResourceNotFoundException("El pedido no existe"));

        mockMvc.perform(get("/api/pedidos/" + idPedido))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // HU20: Actualizar estado del pedido
    // ==========================================

    @Test
    @DisplayName("CP20.1: Avance exitoso del estado operativo del pedido")
    void testConfirmarPedidoExitoso() throws Exception {
        UUID idPedido = UUID.randomUUID();

        Mockito.when(pedidoService.confirmarYEnviar(eq(idPedido)))
                .thenReturn(new PedidoResponse());

        mockMvc.perform(patch("/api/pedidos/" + idPedido + "/confirmar"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CP20.2: Bloqueo por transición de estado no permitida")
    void testConfirmarPedidoInvalido() throws Exception {
        UUID idPedido = UUID.randomUUID();

        Mockito.when(pedidoService.confirmarYEnviar(eq(idPedido)))
                .thenThrow(new BusinessRuleException("Transición de estado no permitida"));

        mockMvc.perform(patch("/api/pedidos/" + idPedido + "/confirmar"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ==========================================
    // HU21: Finalizar entrega del pedido (La que tiene 3 CPs)
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
    @DisplayName("CP21.2: Bloqueo si el pedido no se encuentra en estado válido")
    void testEntregarPedidoErrorEstado() throws Exception {
        UUID idPedido = UUID.randomUUID();

        Mockito.when(pedidoService.marcarEntregado(eq(idPedido)))
                .thenThrow(new BusinessRuleException("El pedido no está listo para servir"));

        mockMvc.perform(patch("/api/pedidos/" + idPedido + "/entregar"))
                .andExpect(status().isUnprocessableEntity());
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
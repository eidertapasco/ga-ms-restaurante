package co.edu.sena.ga_ms_restaurante.mesa.controller;

import co.edu.sena.ga_ms_restaurante.mesa.dto.request.MesaCreateRequest;
import co.edu.sena.ga_ms_restaurante.mesa.dto.request.MesaUpdateRequest;
import co.edu.sena.ga_ms_restaurante.mesa.dto.response.MesaResponse;
import co.edu.sena.ga_ms_restaurante.mesa.enums.EstadoMesa;
import co.edu.sena.ga_ms_restaurante.mesa.service.MesaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MesaController.class)
public class MesaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MesaService mesaService;

    // ==========================================
    // HU1: Consultar mapa de mesas
    // ==========================================

    @Test
    @DisplayName("HU1 - CP1.1: Consultar mesas activas")
    void testConsultarMesasActivas() throws Exception {
        Mockito.when(mesaService.listarMesasActivas())
                .thenReturn(List.of(new MesaResponse()));

        mockMvc.perform(get("/api/mesas"))
                .andExpect(status().isOk());
    }



    @Test
    @DisplayName("HU1 - CP1.4: Consultar mapa sin mesas registradas")
    void testConsultarMesasVacio() throws Exception {
        Mockito.when(mesaService.listarMesasActivas())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/mesas"))
                .andExpect(status().isOk());
    }

    // ==========================================
    // HU2: Asignar Mesas (Cambiando estado a OCUPADA)
    // ==========================================

    @Test
    @DisplayName("HU2 - CP2.1: Asignación exitosa de mesa")
    void testAsignacionExitosa() throws Exception {
        UUID idMesa = UUID.randomUUID();

        // Simula el cambio de estado a OCUPADA
        Mockito.when(mesaService.cambiarEstado(eq(idMesa), any()))
                .thenReturn(new MesaResponse());

        mockMvc.perform(patch("/api/mesas/" + idMesa + "/estado")
                        .param("nuevoEstado", "OCUPADA"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HU2 - CP2.4: Error en asignación de mesa")
    void testAsignacionMesaError() throws Exception {
        UUID idMesa = UUID.randomUUID();

        Mockito.when(mesaService.cambiarEstado(eq(idMesa), any()))
                .thenThrow(new RuntimeException("Error al cambiar estado"));

        mockMvc.perform(patch("/api/mesas/" + idMesa + "/estado")
                        .param("nuevoEstado", "OCUPADA"))
                .andExpect(status().isInternalServerError());
    }

    // ==========================================
    // HU3: Liberar Mesas (Cambiando estado)
    // ==========================================

    @Test
    @DisplayName("HU3 - CP3.1: Liberación exitosa de mesa")
    void testLiberarMesaExitosa() throws Exception {
        UUID idMesa = UUID.randomUUID();

        Mockito.when(mesaService.cambiarEstado(eq(idMesa), any()))
                .thenReturn(new MesaResponse());

        // Asumimos que el estado para liberar es DISPONIBLE o LIBRE
        mockMvc.perform(patch("/api/mesas/" + idMesa + "/estado")
                        .param("nuevoEstado", "LIBRE"))
                .andExpect(status().isOk());
    }

    // ==========================================
    // HU4: Registrar Observaciones (Adaptado a Actualizar)
    // ==========================================
    // NOTA: Como el compañero no programó las observaciones en mesa si no en pedidos, probamos la ruta de actualización
    // para cumplir con la cobertura del controlador.

    @Test
    @DisplayName("HU4 - Adaptación: Actualización exitosa")
    void testActualizarMesaExitosa() throws Exception {
        UUID idMesa = UUID.randomUUID();
        MesaUpdateRequest requestDTO = new MesaUpdateRequest();
        requestDTO.setNombre("Mesa 5");
        requestDTO.setCapacidad(4);

        Mockito.when(mesaService.actualizar(eq(idMesa), any(MesaUpdateRequest.class)))
                .thenReturn(new MesaResponse());

        mockMvc.perform(put("/api/mesas/" + idMesa)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    // ==========================================
    // HU5: Agregar Mesa
    // ==========================================

    @Test
    @DisplayName("HU5 - CP5.1: Registro exitoso de nueva mesa")
    void testAgregarMesaExitosa() throws Exception {
        MesaCreateRequest requestDTO = new MesaCreateRequest();
        requestDTO.setNombre("Mesa VIP");
        requestDTO.setCapacidad(6);
        requestDTO.setZona("Terraza");

        Mockito.when(mesaService.crear(any(MesaCreateRequest.class)))
                .thenReturn(new MesaResponse());

        mockMvc.perform(post("/api/mesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }

    // ==========================================
    // HU6: Eliminar Mesa (Desactivar)
    // ==========================================

    @Test
    @DisplayName("HU6 - CP6.1: Eliminación/Desactivación exitosa")
    void testDesactivarMesaExitosa() throws Exception {
        UUID idMesa = UUID.randomUUID();

        Mockito.when(mesaService.desactivar(idMesa))
                .thenReturn(new MesaResponse());

        mockMvc.perform(patch("/api/mesas/" + idMesa + "/desactivar"))
                .andExpect(status().isOk());
    }
}
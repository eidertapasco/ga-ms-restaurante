package co.edu.sena.ga_ms_restaurante.controller;


import co.edu.sena.ga_ms_restaurante.dto.request.MesaRequestDTO;
import co.edu.sena.ga_ms_restaurante.dto.response.MesaResponseDTO;
import co.edu.sena.ga_ms_restaurante.service.MesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor; // Importamos Lombok
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mesas")
@RequiredArgsConstructor
public class MesaController {

    private final MesaService mesaService; //Inyectamos para enlazar con la logica del service

    // CREAR MESA: POST http://localhost:8080/api/v1/mesas
    @PostMapping
    public ResponseEntity<MesaResponseDTO> crearMesa(@Valid @RequestBody MesaRequestDTO requestDTO) {

        //Pasamos la orden al service y guardamos su respuesta
        MesaResponseDTO response = mesaService.crearMesa(requestDTO);

        //Devolvemos la respuesta al cliente con un codigo de exito 201
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // OBTENER TODAS: GET http://localhost:8080/api/v1/mesas
    @GetMapping
    public ResponseEntity<List<MesaResponseDTO>> listar() {
        return ResponseEntity.ok(mesaService.listarTodas());
    }

    // OBTENER UNA SOLA MESA: GET http://localhost:8080/api/v1/mesas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<MesaResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(mesaService.buscarPorId(id));
    }

    // ACTUALIZAR: PUT http://localhost:8080/api/v1/mesas/{id}
    @PutMapping("/{id}")
    public ResponseEntity<MesaResponseDTO> actualizar(@PathVariable UUID id, @Valid @RequestBody MesaRequestDTO request) {
        return ResponseEntity.ok(mesaService.actualizar(id, request));
    }

    // ELIMINAR: DELETE http://localhost:8080/api/v1/mesas/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        mesaService.eliminar(id);
        return ResponseEntity.noContent().build(); //Devuelve un 204 No Content (exito sin cuerpo)
    }
}

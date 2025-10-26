package ifsp.edu.projeto.cortaai.controller;

import ifsp.edu.projeto.cortaai.dto.BarberDTO;
import ifsp.edu.projeto.cortaai.dto.CreateBarberDTO;
import ifsp.edu.projeto.cortaai.dto.CustomerDTO;
import ifsp.edu.projeto.cortaai.dto.LoginDTO;
import ifsp.edu.projeto.cortaai.service.BarberService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;import ifsp.edu.projeto.cortaai.dto.BarberWorkHoursDTO;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping(value = "/api/barbers", produces = MediaType.APPLICATION_JSON_VALUE)
public class BarberController {

    private final BarberService barberService;

    public BarberController(final BarberService barberService) {
        this.barberService = barberService;
    }

    @GetMapping
    public ResponseEntity<List<BarberDTO>> getAllBarbers() {
        return ResponseEntity.ok(barberService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberDTO> getBarber(@PathVariable(name = "id") final UUID id) {
        return ResponseEntity.ok(barberService.get(id));
    }

    // Endpoint alterado para refletir o registro na plataforma
    @PostMapping("/register")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<UUID> createBarber(@RequestBody @Valid final CreateBarberDTO createBarberDTO) {
        final UUID createdId = barberService.create(createBarberDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<BarberDTO> login(@RequestBody @Valid final LoginDTO loginDTO) {
        final BarberDTO barber = barberService.login(loginDTO);
        return ResponseEntity.ok(barber);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UUID> updateBarber(@PathVariable(name = "id") final UUID id,
                                             @RequestBody @Valid final BarberDTO barberDTO) { // Usa o novo BarberDTO
        barberService.update(id, barberDTO);
        return ResponseEntity.ok(id);
    }

    @PutMapping("/{id}/work-hours")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> setBarberWorkHours(
            @PathVariable(name = "id") final UUID id,
            @RequestBody @Valid final BarberWorkHoursDTO workHoursDTO) {
        barberService.setWorkHours(id, workHoursDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteBarber(@PathVariable(name = "id") final UUID id) {
        barberService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- NOVO ENDPOINT DE UPLOAD ---
    @PostMapping("/{id}/upload-photo")
    public ResponseEntity<String> uploadBarberPhoto(
            @PathVariable(name = "id") final UUID id,
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = barberService.updateBarberProfilePhoto(id, file);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha no upload: " + e.getMessage());
        }
    }

}
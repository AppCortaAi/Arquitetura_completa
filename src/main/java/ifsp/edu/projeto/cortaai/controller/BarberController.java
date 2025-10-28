package ifsp.edu.projeto.cortaai.controller;

import ifsp.edu.projeto.cortaai.dto.*;
import ifsp.edu.projeto.cortaai.service.BarberService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid final LoginDTO loginDTO) { // TIPO DE RETORNO ALTERADO
        final LoginResponseDTO loginResponse = barberService.login(loginDTO); // TIPO DE RETORNO ALTERADO
        return ResponseEntity.ok(loginResponse);
    }

    @PutMapping("/me") // ROTA ALTERADA
    public ResponseEntity<Void> updateBarber(
            Principal principal, // Usuário autenticado injetado
            @RequestBody @Valid final BarberDTO barberDTO) {

        barberService.update(principal.getName(), barberDTO);
        return ResponseEntity.ok().build(); // Retorna 200 OK
    }

    @PutMapping("/me/work-hours")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> setBarberWorkHours(
            Principal principal, // Usuário autenticado injetado
            @RequestBody @Valid final BarberWorkHoursDTO workHoursDTO) {

        barberService.setWorkHours(principal.getName(), workHoursDTO);
        return ResponseEntity.noContent().build();
    }

    // NOVO MÉTODO (movido do BarbershopController)
    @PostMapping("/me/assign-activities")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> assignActivitiesToBarber(
            Principal principal,
            @RequestBody @Valid final BarberActivityAssignDTO assignDTO) {
        // (Requer ROLE_BARBER)
        barberService.assignActivities(principal.getName(), assignDTO);
        return ResponseEntity.noContent().build();
    }

    // NOVO MÉTODO
    @GetMapping("/me/join-requests/history")
    public ResponseEntity<List<JoinRequestHistoryDTO>> getBarberJoinRequestHistory(Principal principal) {
        // (Requer ROLE_BARBER)
        List<JoinRequestHistoryDTO> history = barberService.getJoinRequestHistory(principal.getName()); // Chama o novo método do serviço
        return ResponseEntity.ok(history);
    }

    // NOVO: Endpoint para rejeitar pedido de entrada (adaptado para o BarberController)
    @PostMapping("/me/join-requests/{requestId}/reject") // ROTA ALTERADA para /me para consistência
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> rejectJoinRequest(
            @PathVariable(name = "requestId") final Long requestId,
            Principal principal) {
        // (Requer ROLE_OWNER)
        // O serviço usará o e-mail do principal para validar se ele é o dono
        // da barbearia associada ao pedido antes de rejeitá-lo.
        barberService.rejectJoinRequest(principal.getName(), requestId); // Chama o novo método do serviço
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/me") // ROTA ALTERADA
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteBarber(Principal principal) { // Usuário autenticado injetado
        barberService.delete(principal.getName());
        return ResponseEntity.noContent().build();
    }

    // NOVO: Endpoint para obter a lista de horários disponíveis
    @GetMapping("/{id}/availability")
    public ResponseEntity<List<LocalTime>> getBarberAvailability(
            @PathVariable(name = "id") final UUID id,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
            @RequestParam("duration") final int duration) {

        List<LocalTime> availableSlots = barberService.getAvailableSlots(id, date, duration);
        return ResponseEntity.ok(availableSlots);
    }

    // --- NOVO ENDPOINT DE UPLOAD ---
    @PostMapping("/me/upload-photo") // ROTA ALTERADA
    public ResponseEntity<String> uploadBarberPhoto(
            Principal principal, // Usuário autenticado injetado
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = barberService.updateBarberProfilePhoto(principal.getName(), file);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha no upload: " + e.getMessage());
        }
    }

}
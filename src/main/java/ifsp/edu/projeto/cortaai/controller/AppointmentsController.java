package ifsp.edu.projeto.cortaai.controller;

import ifsp.edu.projeto.cortaai.dto.AppointmentRequestDTO; // NOVO DTO
import ifsp.edu.projeto.cortaai.dto.AppointmentsDTO;
import ifsp.edu.projeto.cortaai.service.AppointmentsService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
public class AppointmentsController {

    private final AppointmentsService appointmentsService;

    public AppointmentsController(final AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentsDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentsService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentsDTO> getAppointments(
            @PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(appointmentsService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createAppointments(
            @RequestBody @Valid final AppointmentRequestDTO appointmentsDTO) { // DTO alterado
        final Long createdId = appointmentsService.create(appointmentsDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateAppointments(@PathVariable(name = "id") final Long id,
                                                   @RequestBody @Valid final AppointmentRequestDTO appointmentsDTO) { // DTO alterado
        appointmentsService.update(id, appointmentsDTO);
        return ResponseEntity.ok(id);
    }

    @PatchMapping("/{id}/cancel")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> cancelAppointments(@PathVariable(name = "id") final Long id) {
        appointmentsService.cancel(id); // Novo método de cancelamento
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteAppointments(@PathVariable(name = "id") final Long id) {
        appointmentsService.delete(id); // Método de exclusão física
        return ResponseEntity.noContent().build();
    }

}
package ifsp.edu.projeto.cortaai.controller;

import ifsp.edu.projeto.cortaai.dto.AppointmentRequestDTO; // NOVO DTO
import ifsp.edu.projeto.cortaai.dto.AppointmentsDTO;
import ifsp.edu.projeto.cortaai.service.AppointmentsService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

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

    @GetMapping("/barbershop/my-shop") // ROTA ALTERADA
    public ResponseEntity<List<AppointmentsDTO>> getAppointmentsForBarbershop(Principal principal) { // ALTERADO
        // (Requer ROLE_OWNER, que já configuramos no JwtAuthorizationFilter)
        return ResponseEntity.ok(appointmentsService.findForBarbershop(principal.getName()));
    }

    @GetMapping("/barber/me") // ROTA ALTERADA
    public ResponseEntity<List<AppointmentsDTO>> getAppointmentsForBarber(Principal principal) { // ALTERADO
        // (Requer ROLE_BARBER)
        return ResponseEntity.ok(appointmentsService.findForBarber(principal.getName()));
    }

    @GetMapping("/customer/me")
    public ResponseEntity<List<AppointmentsDTO>> getAppointmentsForCustomer(Principal principal) {
        // (Requer ROLE_CUSTOMER)
        // O serviço usará o email do principal para buscar os agendamentos do cliente logado
        return ResponseEntity.ok(appointmentsService.findForCustomer(principal.getName()));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createAppointments(
            @RequestBody @Valid final AppointmentRequestDTO appointmentsDTO,
            Principal principal) { // ALTERADO

        // (Requer ROLE_CUSTOMER)
        final Long createdId = appointmentsService.create(appointmentsDTO, principal.getName());
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateAppointments(@PathVariable(name = "id") final Long id,
                                                   @RequestBody @Valid final AppointmentRequestDTO appointmentsDTO,
                                                   Principal principal) { // ALTERADO
        // (Requer ROLE_CUSTOMER)
        appointmentsService.update(id, appointmentsDTO, principal.getName());
        return ResponseEntity.ok(id);
    }

    @PatchMapping("/{id}/cancel")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> cancelAppointments(@PathVariable(name = "id") final Long id,
                                                   Principal principal) { // ALTERADO
        // (Requer ROLE_CUSTOMER ou ROLE_OWNER)
        appointmentsService.cancel(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
    //Endpoint para concluir um agendamento
    @PatchMapping("/{id}/conclude")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> concludeAppointment(@PathVariable(name = "id") final Long id,
                                                    Principal principal) {
        // (Requer ROLE_BARBER)
        appointmentsService.conclude(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteAppointments(@PathVariable(name = "id") final Long id,
                                                   Principal principal) { // ALTERADO
        // (Requer ROLE_OWNER)
        appointmentsService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

}
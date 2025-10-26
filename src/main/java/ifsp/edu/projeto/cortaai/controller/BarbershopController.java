package ifsp.edu.projeto.cortaai.controller;

import ifsp.edu.projeto.cortaai.dto.*;
import ifsp.edu.projeto.cortaai.service.BarberService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ifsp.edu.projeto.cortaai.dto.JoinRequestDTO;
import ifsp.edu.projeto.cortaai.dto.UpdateBarbershopDTO;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class BarbershopController {

    private final BarberService barberService;

    public BarbershopController(final BarberService barberService) {
        this.barberService = barberService;
    }

    // --- Endpoints Públicos (Mosaico de Lojas e Serviços) ---

    @GetMapping("/barbershops")
    public ResponseEntity<List<BarbershopDTO>> listAllBarbershops() {
        return ResponseEntity.ok(barberService.listBarbershops());
    }

    @GetMapping("/barbershops/{shopId}/activities") //servicos da barbearia
    public ResponseEntity<List<ActivityDTO>> listServicesForBarbershop(
            @PathVariable(name = "shopId") final UUID shopId) {
        return ResponseEntity.ok(barberService.listActivities(shopId));
    }

    @GetMapping("/barbershops/{shopId}/barbers")
    public ResponseEntity<List<BarberDTO>> listBarbersForBarbershop(
            @PathVariable(name = "shopId") final UUID shopId) {
        return ResponseEntity.ok(barberService.listBarbersByBarbershop(shopId));
    }

    // --- Fluxo 1: Gestão do Dono (Owner) ---

    @PostMapping("/barbers/{ownerId}/barbershops/register") //get barbershop do owner id
    @ApiResponse(responseCode = "201")
    public ResponseEntity<BarbershopDTO> createBarbershop(
            @PathVariable(name = "ownerId") final UUID ownerId,
            @RequestBody @Valid final CreateBarbershopDTO createBarbershopDTO) {
        final BarbershopDTO createdBarbershop = barberService.createBarbershop(ownerId, createBarbershopDTO);
        return new ResponseEntity<>(createdBarbershop, HttpStatus.CREATED);
    }

    @PostMapping("/barbers/{ownerId}/barbershops/activities")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<ActivityDTO> createActivities(
            @PathVariable(name = "ownerId") final UUID ownerId,
            @RequestBody @Valid final CreateActivityDTO createActivityDTO) {
        final ActivityDTO createdService = barberService.createActivities(ownerId, createActivityDTO);
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }

    @PutMapping("/barbers/{ownerId}/barbershops")
    public ResponseEntity<BarbershopDTO> updateBarbershop(
            @PathVariable(name = "ownerId") final UUID ownerId,
            @RequestBody @Valid final UpdateBarbershopDTO updateBarbershopDTO) {
        final BarbershopDTO updatedBarbershop = barberService.updateBarbershop(ownerId, updateBarbershopDTO);
        return ResponseEntity.ok(updatedBarbershop);
    }

    @DeleteMapping("/barbers/{ownerId}/remove-barber/{barberId}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> removeBarber(
            @PathVariable(name = "ownerId") final UUID ownerId,
            @PathVariable(name = "barberId") final UUID barberId) {
        barberService.removeBarber(ownerId, barberId);
        return ResponseEntity.noContent().build();
    }

    // --- Fluxo 2: Gestão do Staff (Entrada e Habilidades) ---

    @PostMapping("/barbers/{barberId}/join-request")
    @ApiResponse(responseCode = "202") // 202 Accepted (pois requer aprovação)
    public ResponseEntity<Void> requestToJoinBarbershop(
            @PathVariable(name = "barberId") final UUID barberId,
            @RequestBody @Valid final BarberJoinRequestDTO joinRequestDTO) {
        barberService.requestToJoinBarbershop(barberId, joinRequestDTO.getCnpj());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/barbers/{ownerId}/pending-requests")
    public ResponseEntity<List<JoinRequestDTO>> getPendingRequests(@PathVariable(name = "ownerId") final UUID ownerId) {
        return ResponseEntity.ok(barberService.getPendingJoinRequests(ownerId));
    }

    @PostMapping("/barbers/{ownerId}/approve-request/{requestId}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> approveJoinRequest(
            @PathVariable(name = "ownerId") final UUID ownerId,
            @PathVariable(name = "requestId") final Long requestId) {
        barberService.approveJoinRequest(ownerId, requestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/barbers/{barberId}/assign-activities")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> assignActivitiesToBarber(
            @PathVariable(name = "barberId") final UUID barberId,
            @RequestBody @Valid final BarberActivityAssignDTO assignDTO) {
        barberService.assignActivities(barberId, assignDTO);
        return ResponseEntity.noContent().build();
    }

    // --- Fluxo 3: Sair da Loja ---

    @PostMapping("/barbers/{barberId}/free-barber")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> freeBarber(
            @PathVariable(name = "barberId") final UUID barberId) {
        barberService.freeBarber(barberId);
        return ResponseEntity.noContent().build();
    }
}
package ifsp.edu.projeto.cortaai.controller;

import ifsp.edu.projeto.cortaai.dto.*;
import ifsp.edu.projeto.cortaai.service.BarberService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/barbershops/{shopId}/services")
    public ResponseEntity<List<ActivityDTO>> listServicesForBarbershop(
            @PathVariable(name = "shopId") final UUID shopId) {
        return ResponseEntity.ok(barberService.listServices(shopId));
    }

    // --- Fluxo 1: Gestão do Dono (Owner) ---

    @PostMapping("/barbers/{ownerId}/barbershops")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<BarbershopDTO> createBarbershop(
            @PathVariable(name = "ownerId") final UUID ownerId,
            @RequestBody @Valid final CreateBarbershopDTO createBarbershopDTO) {
        final BarbershopDTO createdBarbershop = barberService.createBarbershop(ownerId, createBarbershopDTO);
        return new ResponseEntity<>(createdBarbershop, HttpStatus.CREATED);
    }

    @PostMapping("/barbers/{ownerId}/barbershops/services")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<ActivityDTO> createService(
            @PathVariable(name = "ownerId") final UUID ownerId,
            @RequestBody @Valid final CreateActivityDTO createActivityDTO) {
        final ActivityDTO createdService = barberService.createService(ownerId, createActivityDTO);
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
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

    @PostMapping("/barbers/{ownerId}/approve-request/{requestId}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> approveJoinRequest(
            @PathVariable(name = "ownerId") final UUID ownerId,
            @PathVariable(name = "requestId") final Long requestId) {
        barberService.approveJoinRequest(ownerId, requestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/barbers/{barberId}/assign-services")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> assignServicesToBarber(
            @PathVariable(name = "barberId") final UUID barberId,
            @RequestBody @Valid final BarberServiceAssignDTO assignDTO) {
        barberService.assignServices(barberId, assignDTO);
        return ResponseEntity.noContent().build();
    }

    // --- Fluxo 3: Sair da Loja ---

    @PostMapping("/barbers/{barberId}/leave-barbershop")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> leaveBarbershop(
            @PathVariable(name = "barberId") final UUID barberId) {
        barberService.leaveBarbershop(barberId);
        return ResponseEntity.noContent().build();
    }
}
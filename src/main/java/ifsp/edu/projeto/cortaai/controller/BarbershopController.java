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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
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

    @GetMapping("/barbershops/{shopId}/activities")
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

    @PostMapping("/barbershops/register-my-shop")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<BarbershopDTO> createBarbershop(
            Principal principal, // ALTERADO
            @RequestBody @Valid final CreateBarbershopDTO createBarbershopDTO) {
        // (Requer ROLE_BARBER)
        final BarbershopDTO createdBarbershop = barberService.createBarbershop(principal.getName(), createBarbershopDTO); // ALTERADO
        return new ResponseEntity<>(createdBarbershop, HttpStatus.CREATED);
    }

    // ROTA ALTERADA:
    @PostMapping("/barbershops/my-shop/activities")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<ActivityDTO> createActivities(
            Principal principal, // ALTERADO
            @RequestBody @Valid final CreateActivityDTO createActivityDTO) {
        // (Requer ROLE_OWNER)
        final ActivityDTO createdService = barberService.createActivities(principal.getName(), createActivityDTO); // ALTERADO
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }

    // ROTA ALTERADA:
    @PutMapping("/barbershops/my-shop")
    public ResponseEntity<BarbershopDTO> updateBarbershop(
            Principal principal, // ALTERADO
            @RequestBody @Valid final UpdateBarbershopDTO updateBarbershopDTO) {
        // (Requer ROLE_OWNER)
        final BarbershopDTO updatedBarbershop = barberService.updateBarbershop(principal.getName(), updateBarbershopDTO); // ALTERADO
        return ResponseEntity.ok(updatedBarbershop);
    }

    // ROTA ALTERADA:
    @DeleteMapping("/barbershops/my-shop/remove-barber/{barberId}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> removeBarber(
            Principal principal, // ALTERADO
            @PathVariable(name = "barberId") final UUID barberId) {
        // (Requer ROLE_OWNER)
        barberService.removeBarber(principal.getName(), barberId); // ALTERADO
        return ResponseEntity.noContent().build();
    }

    // --- Fluxo 2: Gestão do Staff (Entrada e Habilidades) ---

    // ROTA ALTERADA:
    @PostMapping("/barbershops/join-request")
    @ApiResponse(responseCode = "202")
    public ResponseEntity<Void> requestToJoinBarbershop(
            Principal principal, // ALTERADO
            @RequestBody @Valid final BarberJoinRequestDTO joinRequestDTO) {
        // (Requer ROLE_BARBER)
        barberService.requestToJoinBarbershop(principal.getName(), joinRequestDTO.getCnpj()); // ALTERADO
        return ResponseEntity.accepted().build();
    }

    // ROTA ALTERADA:
    @GetMapping("/barbershops/my-shop/pending-requests")
    public ResponseEntity<List<JoinRequestDTO>> getPendingRequests(Principal principal) { // ALTERADO
        // (Requer ROLE_OWNER)
        return ResponseEntity.ok(barberService.getPendingJoinRequests(principal.getName())); // ALTERADO
    }

    // ROTA ALTERADA:
    @PostMapping("/barbershops/my-shop/approve-request/{requestId}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> approveJoinRequest(
            Principal principal, // ALTERADO
            @PathVariable(name = "requestId") final Long requestId) {
        // (Requer ROLE_OWNER)
        barberService.approveJoinRequest(principal.getName(), requestId); // ALTERADO
        return ResponseEntity.noContent().build();
    }

    // --- Fluxo 3: Sair da Loja ---

    // ROTA ALTERADA:
    @PostMapping("/barbershops/leave-shop")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> freeBarber(
            Principal principal) { // ALTERADO
        // (Requer ROLE_BARBER)
        barberService.freeBarber(principal.getName()); // ALTERADO
        return ResponseEntity.noContent().build();
    }

    // --- Fluxo 4: Gestao de imagens ---
// ROTA ALTERADA:
    @PostMapping("/barbershops/my-shop/upload-logo")
    public ResponseEntity<String> uploadBarbershopLogo(
            Principal principal, // ALTERADO
            @RequestParam("file") MultipartFile file) {
        // (Requer ROLE_OWNER)
        try {
            String imageUrl = barberService.updateBarbershopLogo(principal.getName(), file); // ALTERADO
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha no upload: " + e.getMessage());
        }
    }

    @PostMapping("/barbershops/my-shop/upload-banner")
    public ResponseEntity<String> uploadBarbershopBanner(
            Principal principal, // ALTERADO
            @RequestParam("file") MultipartFile file) {
        // (Requer ROLE_OWNER)
        try {
            String imageUrl = barberService.updateBarbershopBanner(principal.getName(), file); // ALTERADO
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha no upload: " + e.getMessage());
        }
    }

    // ROTA ALTERADA:
    @PostMapping("/barbershops/my-shop/activities/{activityId}/upload-photo")
    public ResponseEntity<String> uploadActivityPhoto(
            Principal principal, // ALTERADO
            @PathVariable(name = "activityId") final UUID activityId,
            @RequestParam("file") MultipartFile file) {
        // (Requer ROLE_OWNER)
        try {
            String imageUrl = barberService.updateActivityPhoto(principal.getName(), activityId, file); // ALTERADO
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha no upload: " + e.getMessage());
        }
    }

    // ROTA ALTERADA:
    @PostMapping("/barbershops/my-shop/highlights")
    public ResponseEntity<String> addBarbershopHighlight(
            Principal principal, // ALTERADO
            @RequestParam("file") MultipartFile file) {
        // (Requer ROLE_OWNER)
        try {
            String imageUrl = barberService.addBarbershopHighlight(principal.getName(), file); // ALTERADO
            return ResponseEntity.status(HttpStatus.CREATED).body(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Falha no upload: " + e.getMessage());
        }
    }

    // ROTA ALTERADA:
    @DeleteMapping("/barbershops/my-shop/highlights/{highlightId}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteBarbershopHighlight(
            Principal principal, // ALTERADO
            @PathVariable(name = "highlightId") final UUID highlightId) {
        // (Requer ROLE_OWNER)
        barberService.deleteBarbershopHighlight(principal.getName(), highlightId); // ALTERADO
        return ResponseEntity.noContent().build();
    }
}
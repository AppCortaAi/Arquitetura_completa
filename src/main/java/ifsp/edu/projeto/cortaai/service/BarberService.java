package ifsp.edu.projeto.cortaai.service;

import ifsp.edu.projeto.cortaai.dto.*;
import jakarta.validation.Valid;
import ifsp.edu.projeto.cortaai.dto.JoinRequestDTO;
import ifsp.edu.projeto.cortaai.dto.UpdateBarbershopDTO;

import java.util.List;
import java.util.UUID;

public interface BarberService {

    // --- Gestão de Barbeiros (Global) ---
    List<BarberDTO> findAll();
    BarberDTO get(UUID id);
    UUID create(@Valid CreateBarberDTO createBarberDTO); // Registra barbeiro na plataforma
    void update(UUID id, BarberDTO barberDTO); // Atualiza dados do barbeiro
    void delete(UUID id);

    // --- Gestão de Barbearias (Fluxo 1) ---
    BarbershopDTO createBarbershop(UUID ownerBarberId, @Valid CreateBarbershopDTO createBarbershopDTO);
    BarbershopDTO updateBarbershop(UUID ownerId, UpdateBarbershopDTO updateBarbershopDTO);
    BarbershopDTO getBarbershop(UUID barbershopId);
    List<BarbershopDTO> listBarbershops(); // Para o "mosaico" de lojas

    // --- Gestão de Serviços (Fluxo 1) ---
    ActivityDTO createActivities(UUID ownerBarberId, @Valid CreateActivityDTO createActivityDTO);
    List<ActivityDTO> listActivities(UUID barbershopId); // Lista serviços da loja
    List<BarberDTO> listBarbersByBarbershop(UUID barbershopId);

    // --- Gestão de Vínculos (Fluxos 2 e 3) ---
    void requestToJoinBarbershop(UUID barberId, String cnpj);
    void approveJoinRequest(UUID ownerBarberId, Long requestId);
    void freeBarber(UUID barberId);
    void removeBarber(UUID ownerId, UUID barberIdToRemove);
    List<JoinRequestDTO> getPendingJoinRequests(UUID ownerId);

    // --- Gestão de Habilidades (Fluxo 2) ---
    void assignActivities(UUID barberId, @Valid BarberActivityAssignDTO barberActivityAssignDTO);
    void setWorkHours(UUID barberId, BarberWorkHoursDTO workHoursDTO);

    // --- Métodos de validação ---
    boolean tellExists(String tell);
    boolean emailExists(String email);
    boolean documentCPFExists(String documentCPF);

    BarberDTO login(LoginDTO loginDTO);
}
package ifsp.edu.projeto.cortaai.service;

import ifsp.edu.projeto.cortaai.dto.*;
import jakarta.validation.Valid;

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
    BarbershopDTO getBarbershop(UUID barbershopId);
    List<BarbershopDTO> listBarbershops(); // Para o "mosaico" de lojas

    // --- Gestão de Serviços (Fluxo 1) ---
    ActivityDTO createService(UUID ownerBarberId, @Valid CreateActivityDTO createActivityDTO);
    List<ActivityDTO> listServices(UUID barbershopId); // Lista serviços da loja

    // --- Gestão de Vínculos (Fluxos 2 e 3) ---
    void requestToJoinBarbershop(UUID barberId, String cnpj);
    void approveJoinRequest(UUID ownerBarberId, Long requestId);
    void leaveBarbershop(UUID barberId);

    // --- Gestão de Habilidades (Fluxo 2) ---
    void assignServices(UUID barberId, @Valid BarberServiceAssignDTO barberServiceAssignDTO);

    // --- Métodos de validação ---
    boolean tellExists(String tell);
    boolean emailExists(String email);
    boolean documentCPFExists(String documentCPF);

    BarberDTO login(LoginDTO loginDTO);
}
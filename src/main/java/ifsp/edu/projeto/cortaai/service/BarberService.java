package ifsp.edu.projeto.cortaai.service;

import ifsp.edu.projeto.cortaai.dto.*;
import jakarta.validation.Valid;
import ifsp.edu.projeto.cortaai.dto.JoinRequestDTO;
import ifsp.edu.projeto.cortaai.dto.UpdateBarbershopDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate; // NOVO: IMPORT ADICIONADO
import java.time.LocalTime; // NOVO: IMPORT ADICIONADO
import java.util.List;
import java.util.UUID;

public interface BarberService {

    // --- Gestão de Barbeiros (Global) ---
    List<BarberDTO> findAll();
    BarberDTO get(UUID id);
    UUID create(@Valid CreateBarberDTO createBarberDTO);
    void update(UUID id, BarberDTO barberDTO);
    void delete(UUID id);
    BarberDTO login(LoginDTO loginDTO);

    // --- Gestão de Barbearias (Fluxo 1) ---
    BarbershopDTO createBarbershop(UUID ownerBarberId, @Valid CreateBarbershopDTO createBarbershopDTO);
    BarbershopDTO updateBarbershop(UUID ownerId, UpdateBarbershopDTO updateBarbershopDTO);
    BarbershopDTO getBarbershop(UUID barbershopId);
    List<BarbershopDTO> listBarbershops();

    // --- Gestão de Serviços (Fluxo 1) ---
    ActivityDTO createActivities(UUID ownerBarberId, @Valid CreateActivityDTO createActivityDTO);
    List<ActivityDTO> listActivities(UUID barbershopId);
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

    // NOVO: Método para obter horários disponíveis de um barbeiro
    List<LocalTime> getAvailableSlots(UUID barberId, LocalDate date, int durationInMinutes);

    // --- Métodos de validação ---
    boolean tellExists(String tell);
    boolean emailExists(String email);
    boolean documentCPFExists(String documentCPF);

    // --- NOVOS MÉTODOS DE UPLOAD DE IMAGEM ---
    String updateBarberProfilePhoto(UUID barberId, MultipartFile file) throws IOException;
    String updateActivityPhoto(UUID ownerId, UUID activityId, MultipartFile file) throws IOException;
    String updateBarbershopLogo(UUID ownerId, MultipartFile file) throws IOException;
    String updateBarbershopBanner(UUID ownerId, MultipartFile file) throws IOException;
    String addBarbershopHighlight(UUID ownerId, MultipartFile file) throws IOException;
    void deleteBarbershopHighlight(UUID ownerId, UUID highlightId);
}
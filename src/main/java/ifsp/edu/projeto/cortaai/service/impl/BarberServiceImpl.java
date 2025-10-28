package ifsp.edu.projeto.cortaai.service.impl;

import ifsp.edu.projeto.cortaai.dto.*;
import ifsp.edu.projeto.cortaai.events.BeforeDeleteBarber;
import ifsp.edu.projeto.cortaai.exception.NotFoundException;
import ifsp.edu.projeto.cortaai.exception.ReferenceException;
import ifsp.edu.projeto.cortaai.mapper.ActivityMapper;
import ifsp.edu.projeto.cortaai.mapper.BarberMapper;
import ifsp.edu.projeto.cortaai.mapper.BarbershopMapper;
import ifsp.edu.projeto.cortaai.model.*;
import ifsp.edu.projeto.cortaai.model.enums.JoinRequestStatus;
import ifsp.edu.projeto.cortaai.repository.*;
import ifsp.edu.projeto.cortaai.service.BarberService;
import ifsp.edu.projeto.cortaai.service.JwtTokenService;
import ifsp.edu.projeto.cortaai.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class BarberServiceImpl implements BarberService {

    private final BarberRepository barberRepository;
    private final BarbershopRepository barbershopRepository;
    private final BarbershopJoinRequestRepository joinRequestRepository;
    private final ActivityRepository activityRepository;
    private final ApplicationEventPublisher publisher;
    private final BarberMapper barberMapper;
    private final PasswordEncoder passwordEncoder;
    private final BarbershopMapper barbershopMapper;
    private final ActivityMapper activityMapper;

    // DEPENDÊNCIAS ADICIONADAS
    private final StorageService storageService;
    private final BarbershopHighlightRepository barbershopHighlightRepository;
    private final AppointmentsRepository appointmentsRepository;
    private final JwtTokenService jwtTokenService;

    @Value("${app.availability.slot-interval-minutes}")
    private int slotIntervalMinutes;

    public BarberServiceImpl(final BarberRepository barberRepository,
                             final BarbershopRepository barbershopRepository,
                             final BarbershopJoinRequestRepository joinRequestRepository,
                             final ActivityRepository activityRepository,
                             final ApplicationEventPublisher publisher,
                             final BarberMapper barberMapper,
                             final PasswordEncoder passwordEncoder,
                             final BarbershopMapper barbershopMapper,
                             final ActivityMapper activityMapper,
                             final StorageService storageService,
                             final BarbershopHighlightRepository barbershopHighlightRepository,
                             final AppointmentsRepository appointmentsRepository,
                             final JwtTokenService jwtTokenService) {
        this.barberRepository = barberRepository;
        this.barbershopRepository = barbershopRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.activityRepository = activityRepository;
        this.publisher = publisher;
        this.barberMapper = barberMapper;
        this.passwordEncoder = passwordEncoder;
        this.barbershopMapper = barbershopMapper;
        this.activityMapper = activityMapper;
        this.storageService = storageService;
        this.barbershopHighlightRepository = barbershopHighlightRepository; // INJETADO
        this.appointmentsRepository = appointmentsRepository;
        this.jwtTokenService = jwtTokenService;
    }

    // --- Gestão de Barbeiros (Global) ---

    // MÉTODO AUXILIAR NOVO
    private Barber findBarberByEmail(String email) {
        return barberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Barbeiro (usuário autenticado) não encontrado"));
    }

    @Override
    public List<BarberDTO> findAll() {
        return barberRepository.findAll(Sort.by("id")).stream()
                .map(barberMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(final LoginDTO loginDTO) { // TIPO DE RETORNO ALTERADO
        final Barber barber = barberRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new NotFoundException("Usuário ou senha inválidos"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), barber.getPassword())) {
            throw new NotFoundException("Usuário ou senha inválidos");
        }

        // 1. Gera o token JWT para o barbeiro
        final String token = jwtTokenService.generateToken(barber);

        // 2. Mapeia o barbeiro para DTO
        final BarberDTO barberDTO = barberMapper.toDTO(barber);

        // 3. Retorna o LoginResponseDTO
        return LoginResponseDTO.builder()
                .token(token)
                .userData(barberDTO)
                .build();
    }

    @Override
    public BarberDTO get(final UUID id) {
        return barberRepository.findById(id)
                .map(barberMapper::toDTO)
                .orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional
    public UUID create(final CreateBarberDTO createBarberDTO) {
        final Barber barber = new Barber();
        barber.setName(createBarberDTO.getName());
        barber.setTell(createBarberDTO.getTell());
        barber.setEmail(createBarberDTO.getEmail());
        barber.setDocumentCPF(createBarberDTO.getDocumentCPF());
        barber.setPassword(passwordEncoder.encode(createBarberDTO.getPassword()));
        barber.setOwner(false);
        barber.setBarbershop(null);
        return barberRepository.save(barber).getId();
    }

    @Override
    @Transactional
    public void update(final String email, final BarberDTO barberDTO) {
        // Busca o barbeiro pelo e-mail do token
        final Barber barber = findBarberByEmail(email);

        barber.setName(barberDTO.getName());
        barber.setTell(barberDTO.getTell());
        barber.setEmail(barberDTO.getEmail());
        barber.setDocumentCPF(barberDTO.getDocumentCPF());

        barberRepository.save(barber);
    }

    @Override
    @Transactional
    public void delete(final String email) {
        // Busca o barbeiro pelo e-mail do token
        final Barber barber = findBarberByEmail(email);

        publisher.publishEvent(new BeforeDeleteBarber(barber.getId()));
        barberRepository.delete(barber);
    }

    // --- Gestão de Barbearias (Fluxo 1) ---
    @Override
    @Transactional
    public BarbershopDTO createBarbershop(final String ownerEmail, final CreateBarbershopDTO createBarbershopDTO) { // ALTERADO
        final Barber owner = findBarberByEmail(ownerEmail); // ALTERADO

        if (owner.getBarbershop() != null) {
            throw new ReferenceException("Barbeiro já está vinculado a uma barbearia.");
        }

        final Barbershop barbershop = barbershopMapper.toEntity(createBarbershopDTO);
        final Barbershop savedBarbershop = barbershopRepository.save(barbershop);

        owner.setBarbershop(savedBarbershop);
        owner.setOwner(true);
        barberRepository.save(owner);

        return barbershopMapper.toDTO(savedBarbershop);
    }

    @Override
    @Transactional
    public BarbershopDTO updateBarbershop(final String ownerEmail, final UpdateBarbershopDTO updateBarbershopDTO) { // ALTERADO
        final Barber owner = findBarberByEmail(ownerEmail); // ALTERADO

        if (!owner.isOwner() || owner.getBarbershop() == null) {
            throw new ReferenceException("Apenas o dono de uma barbearia pode editar suas informações.");
        }

        final Barbershop barbershop = owner.getBarbershop();

        if (updateBarbershopDTO.getName() != null) {
            barbershop.setName(updateBarbershopDTO.getName());
        }
        if (updateBarbershopDTO.getAddress() != null) {
            barbershop.setAddress(updateBarbershopDTO.getAddress());
        }

        final Barbershop updatedBarbershop = barbershopRepository.save(barbershop);
        return barbershopMapper.toDTO(updatedBarbershop);
    }

    @Override
    public BarbershopDTO getBarbershop(final UUID barbershopId) {
        final Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(NotFoundException::new);
        return barbershopMapper.toDTO(barbershop);
    }

    @Override
    public List<BarbershopDTO> listBarbershops() {
        return barbershopRepository.findAll().stream()
                .map(barbershopMapper::toDTO)
                .toList();
    }

    // --- Gestão de Serviços (Fluxo 1) ---


     // Permite que um barbeiro (dono) crie um novo serviço para sua barbearia.

    @Override
    @Transactional
    public ActivityDTO createActivities(final String ownerEmail, final CreateActivityDTO createActivityDTO) { // ALTERADO
        final Barber owner = findBarberByEmail(ownerEmail); // ALTERADO

        if (!owner.isOwner() || owner.getBarbershop() == null) {
            throw new ReferenceException("Apenas o dono da barbearia pode criar serviços.");
        }

        // Usa o mapper para converter o DTO para a entidade
        final Activity activity = activityMapper.toEntity(createActivityDTO);
        activity.setBarbershop(owner.getBarbershop());
        final Activity savedActivity = activityRepository.save(activity);
        return activityMapper.toDTO(savedActivity);
    }


     // Lista todos os serviços (atividades) disponíveis em uma barbearia específica.

    @Override
    public List<ActivityDTO> listActivities(final UUID barbershopId) {
        // Valida se a barbearia existe antes de buscar os serviços
        if (!barbershopRepository.existsById(barbershopId)) {
            throw new NotFoundException("Barbearia não encontrada.");
        }
        // Busca as atividades e as mapeia para DTOs
        return activityRepository.findByBarbershopId(barbershopId).stream()
                .map(activityMapper::toDTO)
                .toList();
    }

    /**
     * Lista todos os barbeiros que trabalham em uma barbearia específica.
     */
    @Override
    @Transactional(readOnly = true)
    public List<BarberDTO> listBarbersByBarbershop(final UUID barbershopId) {
        if (!barbershopRepository.existsById(barbershopId)) {
            throw new NotFoundException("Barbearia não encontrada.");
        }
        return barberRepository.findByBarbershopId(barbershopId).stream()
                .map(barberMapper::toDTO)
                .toList();
    }

    // --- Gestão de Vínculos (Fluxos 2 e 3) ---

    @Override
    @Transactional
    public void requestToJoinBarbershop(final String barberEmail, final String cnpj) { // ALTERADO
        final Barber barber = findBarberByEmail(barberEmail); // ALTERADO

        if (barber.getBarbershop() != null) {
            throw new ReferenceException("Barbeiro já está em uma barbearia.");
        }

        final Barbershop barbershop = barbershopRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new NotFoundException("Barbearia não encontrada pelo CNPJ"));

        joinRequestRepository.findByBarberIdAndBarbershopId(barber.getId(), barbershop.getId()) // ALTERADO (usa barber.getId())
                .ifPresent(req -> {
                    throw new ReferenceException("Pedido para entrar nesta barbearia já está pendente.");
                });

        final BarbershopJoinRequest request = new BarbershopJoinRequest();
        request.setBarber(barber);
        request.setBarbershop(barbershop);
        request.setStatus(JoinRequestStatus.PENDING);
        joinRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void approveJoinRequest(final String ownerEmail, final Long requestId) { // ALTERADO
        final Barber owner = findBarberByEmail(ownerEmail); // ALTERADO

        final BarbershopJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado"));

        if (!owner.isOwner() || !owner.getBarbershop().getId().equals(request.getBarbershop().getId())) {
            throw new ReferenceException("Apenas o dono desta barbearia pode aprovar pedidos.");
        }

        if(request.getStatus() != JoinRequestStatus.PENDING) {
            throw new ReferenceException("Este pedido não está mais pendente.");
        }

        final Barber barberToJoin = request.getBarber();
        if (barberToJoin.getBarbershop() != null) {
            throw new ReferenceException("O barbeiro já entrou em outra barbearia.");
        }

        barberToJoin.setBarbershop(request.getBarbershop());
        barberRepository.save(barberToJoin);

        joinRequestRepository.delete(request);
    }

    @Override
    @Transactional
    public void freeBarber(final String barberEmail) { // ALTERADO
        final Barber barber = findBarberByEmail(barberEmail); // ALTERADO

        if (barber.getBarbershop() == null) {
            return;
        }
        if (barber.isOwner()) {
            throw new ReferenceException("O dono não pode sair da própria barbearia (deve excluí-la ou passar a posse).");
        }

        barber.setBarbershop(null);
        barber.getActivities().clear();
        barberRepository.save(barber);
    }

    @Override
    @Transactional
    public void removeBarber(final String ownerEmail, final UUID barberIdToRemove) { // ALTERADO
        final Barber owner = findBarberByEmail(ownerEmail); // ALTERADO

        final Barber barberToRemove = barberRepository.findById(barberIdToRemove)
                .orElseThrow(() -> new NotFoundException("Barbeiro a ser removido não encontrado"));

        if (!owner.isOwner() || owner.getBarbershop() == null) {
            throw new ReferenceException("Apenas o dono da barbearia pode remover barbeiros.");
        }

        if (barberToRemove.getBarbershop() == null || !barberToRemove.getBarbershop().getId().equals(owner.getBarbershop().getId())) {
            throw new ReferenceException("O barbeiro informado não pertence a esta barbearia.");
        }

        if(owner.getId().equals(barberToRemove.getId())) {
            throw new ReferenceException("O dono não pode remover a si mesmo.");
        }

        barberToRemove.setBarbershop(null);
        barberToRemove.getActivities().clear();
        barberRepository.save(barberToRemove);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JoinRequestDTO> getPendingJoinRequests(final String ownerEmail) { // ALTERADO
        final Barber owner = findBarberByEmail(ownerEmail); // ALTERADO

        if (!owner.isOwner() || owner.getBarbershop() == null) {
            throw new ReferenceException("Apenas o dono de uma barbearia pode ver os pedidos pendentes.");
        }

        final UUID barbershopId = owner.getBarbershop().getId();

        final List<BarbershopJoinRequest> requests = joinRequestRepository
                .findByBarbershopIdAndStatus(barbershopId, JoinRequestStatus.PENDING);

        return requests.stream()
                .map(request -> {
                    JoinRequestDTO dto = new JoinRequestDTO();
                    dto.setRequestId(request.getId());

                    BarberInfoDTO barberInfo = new BarberInfoDTO();
                    barberInfo.setId(request.getBarber().getId());
                    barberInfo.setName(request.getBarber().getName());
                    barberInfo.setEmail(request.getBarber().getEmail());
                    barberInfo.setTell(request.getBarber().getTell());

                    dto.setBarber(barberInfo);
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JoinRequestHistoryDTO> getJoinRequestHistory(final String barberEmail) { // ALTERADO para usar email do Principal
        final Barber barber = findBarberByEmail(barberEmail); // Usa o helper existente para obter o barbeiro autenticado

        final List<BarbershopJoinRequest> requests = joinRequestRepository.findByBarberId(barber.getId());

        // Mapeia a lista de entidades para a lista de DTOs
        return requests.stream()
                .map(request -> {
                    JoinRequestHistoryDTO dto = new JoinRequestHistoryDTO();
                    dto.setRequestId(request.getId());
                    dto.setStatus(request.getStatus());
                    if (request.getBarbershop() != null) {
                        dto.setBarbershopId(request.getBarbershop().getId());
                        dto.setBarbershopName(request.getBarbershop().getName());
                    }
                    return dto;
                })
                .toList(); // Alterado de collect(Collectors.toList()) para toList()
    }

    @Override
    @Transactional
    public void rejectJoinRequest(final String ownerEmail, final Long requestId) { // ALTERADO para usar email do Principal
        final Barber owner = findBarberByEmail(ownerEmail); // Usa o helper existente para obter o dono autenticado

        final BarbershopJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado"));

        // Valida se quem está recusando é o dono da barbearia para a qual o pedido foi feito
        // Adicionado owner.getBarbershop() == null para evitar NullPointerException antes de chamar .getId()
        if (!owner.isOwner() || owner.getBarbershop() == null || !owner.getBarbershop().getId().equals(request.getBarbershop().getId())) {
            throw new ReferenceException("Apenas o dono desta barbearia pode recusar pedidos.");
        }

        // Valida se o pedido ainda está pendente
        if(request.getStatus() != JoinRequestStatus.PENDING) {
            throw new ReferenceException("Este pedido não está mais pendente.");
        }

        request.setStatus(JoinRequestStatus.REJECTED);
        joinRequestRepository.save(request);
    }


    // --- Gestão de Habilidades (Fluxo 2) ---

    @Override
    @Transactional
    public void assignActivities(final String barberEmail, final BarberActivityAssignDTO barberActivityAssignDTO) { // ALTERADO
        final Barber barber = findBarberByEmail(barberEmail); // ALTERADO

        if (barber.getBarbershop() == null) {
            throw new ReferenceException("Barbeiro não está em uma barbearia para vincular serviços.");
        }

        final UUID barbershopId = barber.getBarbershop().getId();

        final Set<Activity> servicesToAssign = new HashSet<>(
                activityRepository.findAllById(barberActivityAssignDTO.getActivityIds())
        );

        for (Activity s : servicesToAssign) {
            if (!s.getBarbershop().getId().equals(barbershopId)) {
                throw new ReferenceException("Serviço ID " + s.getId() + " não pertence à barbearia deste barbeiro.");
            }
        }

        barber.setActivities(servicesToAssign);
        barberRepository.save(barber);
    }

    @Override
    @Transactional
    public void setWorkHours(final String email, final BarberWorkHoursDTO workHoursDTO) {
        // Busca o barbeiro pelo e-mail do token
        final Barber barber = findBarberByEmail(email);

        if (workHoursDTO.getWorkStartTime().isAfter(workHoursDTO.getWorkEndTime())) {
            throw new ReferenceException("O horário de início do expediente deve ser anterior ao de término.");
        }

        barber.setWorkStartTime(workHoursDTO.getWorkStartTime());
        barber.setWorkEndTime(workHoursDTO.getWorkEndTime());

        barberRepository.save(barber);
    }

    // --- Métodos de validação ---

    @Override
    public boolean tellExists(final String tell) {
        return barberRepository.existsByTellIgnoreCase(tell);
    }

    @Override
    public boolean emailExists(final String email) {
        return barberRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean documentCPFExists(final String documentCPF) {
        return barberRepository.existsByDocumentCPFIgnoreCase(documentCPF);
    }

    @Override
    @Transactional
    public String updateBarberProfilePhoto(String email, MultipartFile file) throws IOException {
        final Barber barber = findBarberByEmail(email);

        // 1. Deletar foto antiga
        String oldPublicId = barber.getImageUrlPublicId();
        if (oldPublicId != null) {
            storageService.deleteFile(oldPublicId);
        }

        // 2. Faz o upload
        final UploadResultDTO uploadResult = storageService.uploadFile(file, "barber-profiles");

        // 3. Salva
        barber.setImageUrl(uploadResult.getSecureUrl());
        barber.setImageUrlPublicId(uploadResult.getPublicId());
        barberRepository.save(barber);
        return uploadResult.getSecureUrl();
    }

    @Override
    @Transactional
    public String updateActivityPhoto(String ownerEmail, UUID activityId, MultipartFile file) throws IOException {
        final Barbershop barbershop = getBarbershopFromOwner(ownerEmail);

        final Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Serviço (Activity) não encontrado"));
        if (!activity.getBarbershop().getId().equals(barbershop.getId())) {
            throw new ReferenceException("Este serviço não pertence à sua barbearia.");
        }

        // 1. Deletar foto antiga
        String oldPublicId = activity.getImageUrlPublicId();
        if (oldPublicId != null) {
            storageService.deleteFile(oldPublicId);
        }

        // 2. Upload
        final UploadResultDTO uploadResult = storageService.uploadFile(file, "activity-images");

        // 3. Salva
        activity.setImageUrl(uploadResult.getSecureUrl());
        activity.setImageUrlPublicId(uploadResult.getPublicId());
        activityRepository.save(activity);
        return uploadResult.getSecureUrl();
    }

    @Override
    @Transactional
    public String updateBarbershopLogo(String ownerEmail, MultipartFile file) throws IOException {
        final Barbershop barbershop = getBarbershopFromOwner(ownerEmail);

        // 1. Deletar logo antigo
        String oldPublicId = barbershop.getLogoUrlPublicId();
        if (oldPublicId != null) {
            storageService.deleteFile(oldPublicId);
        }

        // 2. Upload
        final UploadResultDTO uploadResult = storageService.uploadFile(file, "barbershop-logos");

        // 3. Salva
        barbershop.setLogoUrl(uploadResult.getSecureUrl());
        barbershop.setLogoUrlPublicId(uploadResult.getPublicId());
        barbershopRepository.save(barbershop);
        return uploadResult.getSecureUrl();
    }

    @Override
    @Transactional
    public String updateBarbershopBanner(String ownerEmail, MultipartFile file) throws IOException {
        final Barbershop barbershop = getBarbershopFromOwner(ownerEmail);

        // 1. Deletar banner antigo
        String oldPublicId = barbershop.getBannerUrlPublicId();
        if (oldPublicId != null) {
            storageService.deleteFile(oldPublicId);
        }

        // 2. Upload
        final UploadResultDTO uploadResult = storageService.uploadFile(file, "barbershop-banners");

        // 3. Salva
        barbershop.setBannerUrl(uploadResult.getSecureUrl());
        barbershop.setBannerUrlPublicId(uploadResult.getPublicId());
        barbershopRepository.save(barbershop);
        return uploadResult.getSecureUrl();
    }

    @Override
    @Transactional
    public String addBarbershopHighlight(String ownerEmail, MultipartFile file) throws IOException {
        final Barbershop barbershop = getBarbershopFromOwner(ownerEmail);

        // 1. Upload (não há foto antiga para deletar aqui)
        final UploadResultDTO uploadResult = storageService.uploadFile(file, "barbershop-highlights");

        BarbershopHighlight highlight = new BarbershopHighlight();
        highlight.setBarbershop(barbershop);
        highlight.setImageUrl(uploadResult.getSecureUrl());
        highlight.setImageUrlPublicId(uploadResult.getPublicId()); // Salva o Public ID

        barbershopHighlightRepository.save(highlight);
        return uploadResult.getSecureUrl();
    }

    @Override
    @Transactional
    public void deleteBarbershopHighlight(String ownerEmail, UUID highlightId) {
        final Barbershop barbershop = getBarbershopFromOwner(ownerEmail);

        final BarbershopHighlight highlight = barbershopHighlightRepository.findById(highlightId)
                .orElseThrow(() -> new NotFoundException("Imagem de destaque não encontrada"));

        if (!highlight.getBarbershop().getId().equals(barbershop.getId())) {
            throw new ReferenceException("Esta imagem não pertence à sua barbearia.");
        }

        // 1. Deletar a imagem do Cloudinary
        String publicId = highlight.getImageUrlPublicId();
        if (publicId != null) {
            try {
                storageService.deleteFile(publicId);
            } catch (IOException e) {
                // Em um app real, logaríamos o erro, mas não impediríamos a exclusão do banco
                // logger.error("Falha ao deletar arquivo do Cloudinary: " + publicId, e);
            }
        }

        // 2. Deletar a entidade do banco
        barbershopHighlightRepository.delete(highlight);
    }


    // --- NOVO MÉTODO DE APOIO ---

    /**
     * Valida se o ID pertence a um dono e retorna a barbearia dele.
     */
    private Barbershop getBarbershopFromOwner(String ownerEmail) { // NOVA ASSINATURA
        final Barber owner = findBarberByEmail(ownerEmail); // Usa o helper existente

        if (!owner.isOwner() || owner.getBarbershop() == null) {
            throw new ReferenceException("Apenas o dono de uma barbearia pode realizar esta ação.");
        }

        return owner.getBarbershop();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(UUID barberId, LocalDate date, int durationInMinutes) {
        final Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado"));

        if (barber.getWorkStartTime() == null || barber.getWorkEndTime() == null) {
            return new ArrayList<>(); // Expediente não definido
        }

        // 1. Definir os limites do dia de trabalho
        final LocalTime workStart = barber.getWorkStartTime();
        final LocalTime workEnd = barber.getWorkEndTime();

        // 2. Buscar agendamentos existentes (apenas os agendados ou concluídos)
        OffsetDateTime startOfDay = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = date.atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        List<Appointments> existingAppointments = appointmentsRepository
                .findByBarberIdAndStartTimeBetween(barberId, startOfDay, endOfDay)
                .stream()
                // Filtra agendamentos que não estão cancelados
                .filter(a -> a.getStatus() != ifsp.edu.projeto.cortaai.model.enums.AppointmentStatus.CANCELLED)
                // Ordena pela hora de início
                .sorted(Comparator.comparing(Appointments::getStartTime))
                .toList();

        // 3. Criar uma lista de "blocos livres" (gaps)
        List<LocalTime[]> freeBlocks = new ArrayList<>();
        LocalTime currentPointer = workStart;

        for (Appointments appointment : existingAppointments) {
            LocalTime appointmentStart = appointment.getStartTime().toLocalTime();
            LocalTime appointmentEnd = appointment.getEndTime().toLocalTime();

            // Se houver um vão entre o ponteiro atual e o início do agendamento
            if (currentPointer.isBefore(appointmentStart)) {
                freeBlocks.add(new LocalTime[]{currentPointer, appointmentStart});
            }
            // Avança o ponteiro para o fim do agendamento atual
            currentPointer = appointmentEnd;
        }

        // Adiciona o último bloco (do fim do último agendamento até o fim do expediente)
        if (currentPointer.isBefore(workEnd)) {
            freeBlocks.add(new LocalTime[]{currentPointer, workEnd});
        }

        // 4. Verificar quais slots cabem nos blocos livres
        List<LocalTime> availableSlots = new ArrayList<>();
        long durationLong = (long) durationInMinutes; // Converte para long para aritmética

        for (LocalTime[] block : freeBlocks) {
            LocalTime blockStart = block[0];
            LocalTime blockEnd = block[1];

            // Itera dentro do bloco livre usando o intervalo definido no application.yml
            LocalTime potentialSlotStart = blockStart;

            while (potentialSlotStart.isBefore(blockEnd)) {
                LocalTime potentialSlotEnd = potentialSlotStart.plusMinutes(durationLong);

                // O slot cabe se:
                // 1. O fim do slot não ultrapassa o fim do bloco
                // 2. O fim do slot não ultrapassa o fim do expediente
                if (!potentialSlotEnd.isAfter(blockEnd) && !potentialSlotEnd.isAfter(workEnd)) {
                    availableSlots.add(potentialSlotStart);
                }

                // Avança para o próximo ponto de verificação
                potentialSlotStart = potentialSlotStart.plusMinutes(slotIntervalMinutes);
            }
        }

        return availableSlots;
    }
    

    
}
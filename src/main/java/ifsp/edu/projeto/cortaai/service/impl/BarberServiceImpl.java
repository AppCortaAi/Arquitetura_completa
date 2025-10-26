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
import ifsp.edu.projeto.cortaai.service.StorageService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    public BarberServiceImpl(final BarberRepository barberRepository,
                             final BarbershopRepository barbershopRepository,
                             final BarbershopJoinRequestRepository joinRequestRepository,
                             final ActivityRepository activityRepository,
                             final ApplicationEventPublisher publisher,
                             final BarberMapper barberMapper,
                             final PasswordEncoder passwordEncoder,
                             final BarbershopMapper barbershopMapper,
                             final ActivityMapper activityMapper,
                             final StorageService storageService, // ADICIONADO AO CONSTRUTOR
                             final BarbershopHighlightRepository barbershopHighlightRepository) { // ADICIONADO AO CONSTRUTOR
        this.barberRepository = barberRepository;
        this.barbershopRepository = barbershopRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.activityRepository = activityRepository;
        this.publisher = publisher;
        this.barberMapper = barberMapper;
        this.passwordEncoder = passwordEncoder;
        this.barbershopMapper = barbershopMapper;
        this.activityMapper = activityMapper;
        this.storageService = storageService; // INJETADO
        this.barbershopHighlightRepository = barbershopHighlightRepository; // INJETADO
    }

    // --- Gestão de Barbeiros (Global) ---

    @Override
    public List<BarberDTO> findAll() {
        return barberRepository.findAll(Sort.by("id")).stream()
                .map(barberMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BarberDTO login(final LoginDTO loginDTO) {
        final Barber barber = barberRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new NotFoundException("Usuário ou senha inválidos"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), barber.getPassword())) {
            throw new NotFoundException("Usuário ou senha inválidos");
        }

        return barberMapper.toDTO(barber);
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
    public void update(final UUID id, final BarberDTO barberDTO) {
        final Barber barber = barberRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        barber.setName(barberDTO.getName());
        barber.setTell(barberDTO.getTell());
        barber.setEmail(barberDTO.getEmail());
        barber.setDocumentCPF(barberDTO.getDocumentCPF());

        barberRepository.save(barber);
    }

    @Override
    @Transactional
    public void delete(final UUID id) {
        final Barber barber = barberRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteBarber(id));
        barberRepository.delete(barber);
    }

    // --- Gestão de Barbearias (Fluxo 1) ---

    @Override
    @Transactional
    public BarbershopDTO createBarbershop(final UUID ownerBarberId, final CreateBarbershopDTO createBarbershopDTO) {
        final Barber owner = barberRepository.findById(ownerBarberId)
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado"));

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
    public BarbershopDTO updateBarbershop(final UUID ownerId, final UpdateBarbershopDTO updateBarbershopDTO) {
        final Barber owner = barberRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Barbeiro (Dono) não encontrado"));

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

    /**
     * Permite que um barbeiro (dono) crie um novo serviço para sua barbearia.
     */
    @Override
    @Transactional
    public ActivityDTO createActivities(final UUID ownerBarberId, final CreateActivityDTO createActivityDTO) {
        final Barber owner = barberRepository.findById(ownerBarberId)
                .orElseThrow(() -> new NotFoundException("Barbeiro (Dono) não encontrado"));

        if (!owner.isOwner() || owner.getBarbershop() == null) {
            throw new ReferenceException("Apenas o dono da barbearia pode criar serviços.");
        }

        // Usa o mapper para converter o DTO para a entidade
        final Activity activity = activityMapper.toEntity(createActivityDTO);
        activity.setBarbershop(owner.getBarbershop()); // Associa a atividade à barbearia do dono

        final Activity savedActivity = activityRepository.save(activity);

        // Retorna o DTO da atividade criada
        return activityMapper.toDTO(savedActivity);
    }

    /**
     * Lista todos os serviços (atividades) disponíveis em uma barbearia específica.
     */
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
    public void requestToJoinBarbershop(final UUID barberId, final String cnpj) {
        final Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado"));

        if (barber.getBarbershop() != null) {
            throw new ReferenceException("Barbeiro já está em uma barbearia.");
        }

        final Barbershop barbershop = barbershopRepository.findByCnpj(cnpj)
                .orElseThrow(() -> new NotFoundException("Barbearia não encontrada pelo CNPJ"));

        joinRequestRepository.findByBarberIdAndBarbershopId(barberId, barbershop.getId())
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
    public void approveJoinRequest(final UUID ownerBarberId, final Long requestId) {
        final Barber owner = barberRepository.findById(ownerBarberId)
                .orElseThrow(() -> new NotFoundException("Dono não encontrado"));

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
    public void freeBarber(final UUID barberId) {
        final Barber barber = barberRepository.findById(barberId)
                .orElseThrow(NotFoundException::new);

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
    public void removeBarber(final UUID ownerId, final UUID barberIdToRemove) {
        final Barber owner = barberRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Barbeiro (Dono) não encontrado"));

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
    public List<JoinRequestDTO> getPendingJoinRequests(final UUID ownerId) {
        final Barber owner = barberRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Barbeiro (Dono) não encontrado"));

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

    // --- Gestão de Habilidades (Fluxo 2) ---

    @Override
    @Transactional
    public void assignActivities(final UUID barberId, final BarberActivityAssignDTO barberActivityAssignDTO) {
        final Barber barber = barberRepository.findById(barberId)
                .orElseThrow(NotFoundException::new);

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
    public void setWorkHours(final UUID barberId, final BarberWorkHoursDTO workHoursDTO) {
        final Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado"));

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
    public String updateBarberProfilePhoto(UUID barberId, MultipartFile file) throws IOException {
        final Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado"));

        final String imageUrl = storageService.uploadFile(file, "barber-profiles");

        barber.setImageUrl(imageUrl);
        barberRepository.save(barber);
        return imageUrl;
    }

    @Override
    @Transactional
    public String updateActivityPhoto(UUID ownerId, UUID activityId, MultipartFile file) throws IOException {
        final Barbershop barbershop = getBarbershopFromOwner(ownerId);

        final Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Serviço (Activity) não encontrado"));

        if (!activity.getBarbershop().getId().equals(barbershop.getId())) {
            throw new ReferenceException("Este serviço não pertence à sua barbearia.");
        }

        final String imageUrl = storageService.uploadFile(file, "activity-images");

        activity.setImageUrl(imageUrl);
        activityRepository.save(activity);
        return imageUrl;
    }

    @Override
    @Transactional
    public String updateBarbershopLogo(UUID ownerId, MultipartFile file) throws IOException {
        final Barbershop barbershop = getBarbershopFromOwner(ownerId);
        final String imageUrl = storageService.uploadFile(file, "barbershop-logos");

        barbershop.setLogoUrl(imageUrl);
        barbershopRepository.save(barbershop);
        return imageUrl;
    }

    @Override
    @Transactional
    public String updateBarbershopBanner(UUID ownerId, MultipartFile file) throws IOException {
        final Barbershop barbershop = getBarbershopFromOwner(ownerId);
        final String imageUrl = storageService.uploadFile(file, "barbershop-banners");

        barbershop.setBannerUrl(imageUrl);
        barbershopRepository.save(barbershop);
        return imageUrl;
    }

    @Override
    @Transactional
    public String addBarbershopHighlight(UUID ownerId, MultipartFile file) throws IOException {
        final Barbershop barbershop = getBarbershopFromOwner(ownerId);
        final String imageUrl = storageService.uploadFile(file, "barbershop-highlights");

        BarbershopHighlight highlight = new BarbershopHighlight();
        highlight.setBarbershop(barbershop);
        highlight.setImageUrl(imageUrl);

        barbershopHighlightRepository.save(highlight);
        return imageUrl;
    }

    @Override
    @Transactional
    public void deleteBarbershopHighlight(UUID ownerId, UUID highlightId) {
        final Barbershop barbershop = getBarbershopFromOwner(ownerId);

        final BarbershopHighlight highlight = barbershopHighlightRepository.findById(highlightId)
                .orElseThrow(() -> new NotFoundException("Imagem de destaque não encontrada"));

        if (!highlight.getBarbershop().getId().equals(barbershop.getId())) {
            throw new ReferenceException("Esta imagem não pertence à sua barbearia.");
        }

        // NOTA: Idealmente, você também deve deletar a imagem do Cloudinary aqui.
        // Isso requer uma lógica mais complexa no seu StorageService.

        barbershopHighlightRepository.delete(highlight);
    }


    // --- NOVO MÉTODO DE APOIO ---

    /**
     * Valida se o ID pertence a um dono e retorna a barbearia dele.
     */
    private Barbershop getBarbershopFromOwner(UUID ownerId) {
        final Barber owner = barberRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Barbeiro (Dono) não encontrado"));

        if (!owner.isOwner() || owner.getBarbershop() == null) {
            throw new ReferenceException("Apenas o dono de uma barbearia pode realizar esta ação.");
        }

        return owner.getBarbershop();
    }
}
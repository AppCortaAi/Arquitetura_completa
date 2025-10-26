package ifsp.edu.projeto.cortaai.service.impl;

import ifsp.edu.projeto.cortaai.dto.*;
import ifsp.edu.projeto.cortaai.events.BeforeDeleteBarber;
import ifsp.edu.projeto.cortaai.exception.NotFoundException;
import ifsp.edu.projeto.cortaai.exception.ReferenceException; // Usado para regras de negócio
import ifsp.edu.projeto.cortaai.mapper.BarberMapper;
import ifsp.edu.projeto.cortaai.model.*;
import ifsp.edu.projeto.cortaai.model.enums.JoinRequestStatus;
import ifsp.edu.projeto.cortaai.repository.*;
import ifsp.edu.projeto.cortaai.service.BarberService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ifsp.edu.projeto.cortaai.dto.BarberInfoDTO;
import ifsp.edu.projeto.cortaai.dto.JoinRequestDTO;
import ifsp.edu.projeto.cortaai.dto.UpdateBarbershopDTO;
import ifsp.edu.projeto.cortaai.mapper.BarbershopMapper;


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

    // Mappers que serão criados na próxima etapa
    // private final BarbershopMapper barbershopMapper;
    // private final ServiceMapper serviceMapper;

    public BarberServiceImpl(final BarberRepository barberRepository,
                             final BarbershopRepository barbershopRepository,
                             final BarbershopJoinRequestRepository joinRequestRepository,
                             final ActivityRepository activityRepository,
                             final ApplicationEventPublisher publisher,
                             final BarberMapper barberMapper,
                             final PasswordEncoder passwordEncoder,
                             final BarbershopMapper barbershopMapper) {
        this.barberRepository = barberRepository;
        this.barbershopRepository = barbershopRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.activityRepository = activityRepository;
        this.publisher = publisher;
        this.barberMapper = barberMapper;
        this.passwordEncoder = passwordEncoder;
        this.barbershopMapper = barbershopMapper;
    }

    // --- Gestão de Barbeiros (Global) ---

    @Override
    public List<BarberDTO> findAll() {
        return barberRepository.findAll(Sort.by("id")).stream()
                .map(barberMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional // Garante que a transação seja apenas de leitura
    public BarberDTO login(final LoginDTO loginDTO) {
        final Barber barber = barberRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new NotFoundException("Usuário ou senha inválidos"));

        // Compara a senha enviada com o hash salvo no banco
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
        barber.setBarbershop(null); // Entra na plataforma "livre"
        return barberRepository.save(barber).getId();
    }

    @Override
    @Transactional
    public void update(final UUID id, final BarberDTO barberDTO) {
        final Barber barber = barberRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        // Mapeamento manual para não alterar senha ou vínculos
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

        // CORREÇÃO: Usar o mapper para criar a entidade a partir do DTO
        final Barbershop barbershop = barbershopMapper.toEntity(createBarbershopDTO);

        // Salva a nova barbearia no banco de dados
        final Barbershop savedBarbershop = barbershopRepository.save(barbershop);

        // Vincula o barbeiro como dono da barbearia recém-criada
        owner.setBarbershop(savedBarbershop);
        owner.setOwner(true);
        barberRepository.save(owner);

        // Retorna o DTO da barbearia criada
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

        // Atualiza os campos apenas se eles foram enviados no DTO
        if (updateBarbershopDTO.getName() != null) {
            barbershop.setName(updateBarbershopDTO.getName());
        }
        if (updateBarbershopDTO.getAddress() != null) {
            barbershop.setAddress(updateBarbershopDTO.getAddress());
        }

        final Barbershop updatedBarbershop = barbershopRepository.save(barbershop);

        // TODO: Este trecho precisa ser descomentado quando o BarbershopMapper for implementado
        // return barbershopMapper.toDTO(updatedBarbershop);
        // Por enquanto, vamos lançar a exceção para lembrar da pendência
        throw new UnsupportedOperationException("BarbershopMapper ainda não implementado");
    }

    @Override
    public BarbershopDTO getBarbershop(final UUID barbershopId) {
        // TODO: Implementar com BarbershopMapper
        // final Barbershop barbershop = barbershopRepository.findById(barbershopId).orElseThrow(NotFoundException::new);
        // return barbershopMapper.toDTO(barbershop);
        throw new UnsupportedOperationException("BarbershopMapper ainda não implementado");
    }

    @Override
    public List<BarbershopDTO> listBarbershops() {
        // TODO: Implementar com BarbershopMapper
        // return barbershopRepository.findAll().stream().map(barbershopMapper::toDTO).toList();
        throw new UnsupportedOperationException("BarbershopMapper ainda não implementado");
    }

    // --- Gestão de Serviços (Fluxo 1) ---

    @Override
    @Transactional
    public ActivityDTO createActivities(final UUID ownerBarberId, final CreateActivityDTO createActivityDTO) {
        final Barber owner = barberRepository.findById(ownerBarberId)
                .orElseThrow(() -> new NotFoundException("Barbeiro (Dono) não encontrado"));

        if (!owner.isOwner() || owner.getBarbershop() == null) {
            throw new ReferenceException("Apenas o dono da barbearia pode criar serviços.");
        }

        final Activity activity = new Activity();
        activity.setBarbershop(owner.getBarbershop());
        activity.setActivityName(createActivityDTO.getActivityName());
        activity.setPrice(createActivityDTO.getPrice());
        activity.setDurationMinutes(createActivityDTO.getDurationMinutes());
        final Activity savedActivity = activityRepository.save(activity);

        // TODO: Retornar DTO usando o ServiceMapper
        // return serviceMapper.toDTO(savedService);
        throw new UnsupportedOperationException("ServiceMapper ainda não implementado");
    }

    @Override
    public List<ActivityDTO> listActivities(final UUID barbershopId) {
        // TODO: Implementar com ServiceMapper
        // return serviceRepository.findByBarbershopId(barbershopId).stream().map(serviceMapper::toDTO).toList();
        throw new UnsupportedOperationException("ServiceMapper ainda não implementado");
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

        // Verifica se já não existe um pedido pendente
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

        joinRequestRepository.delete(request); // Deleta o pedido após aprovação
    }

    @Override
    @Transactional
    public void freeBarber(final UUID barberId) {
        final Barber barber = barberRepository.findById(barberId)
                .orElseThrow(NotFoundException::new);

        if (barber.getBarbershop() == null) {
            return; // Já está livre
        }

        if (barber.isOwner()) {
            throw new ReferenceException("O dono não pode sair da própria barbearia (deve excluí-la ou passar a posse).");
        }

        barber.setBarbershop(null);
        barber.getActivities().clear(); // Limpa os vínculos de serviço (Regra D. Quebra de Vínculo)
        barberRepository.save(barber);
    }
    @Override
    @Transactional(readOnly = true)
    public List<JoinRequestDTO> getPendingJoinRequests(final UUID ownerId) {
        final Barber owner = barberRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Barbeiro (Dono) não encontrado"));

        // Valida se o usuário é dono e possui uma barbearia
        if (!owner.isOwner() || owner.getBarbershop() == null) {
            throw new ReferenceException("Apenas o dono de uma barbearia pode ver os pedidos pendentes.");
        }

        final UUID barbershopId = owner.getBarbershop().getId();

        // Busca os pedidos com status PENDING para a barbearia do dono
        final List<BarbershopJoinRequest> requests = joinRequestRepository
                .findByBarbershopIdAndStatus(barbershopId, JoinRequestStatus.PENDING);

        // Mapeia as entidades para os DTOs de resposta
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

        // Busca os serviços e valida se pertencem à loja correta
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
    public void removeBarber(final UUID ownerId, final UUID barberIdToRemove) {
        final Barber owner = barberRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Barbeiro (Dono) não encontrado"));

        final Barber barberToRemove = barberRepository.findById(barberIdToRemove)
                .orElseThrow(() -> new NotFoundException("Barbeiro a ser removido não encontrado"));

        // 1. Valida se quem está executando é de fato um dono de barbearia
        if (!owner.isOwner() || owner.getBarbershop() == null) {
            throw new ReferenceException("Apenas o dono da barbearia pode remover barbeiros.");
        }

        // 2. Valida se o barbeiro a ser removido pertence à barbearia do dono
        if (barberToRemove.getBarbershop() == null || !barberToRemove.getBarbershop().getId().equals(owner.getBarbershop().getId())) {
            throw new ReferenceException("O barbeiro informado não pertence a esta barbearia.");
        }

        // 3. O dono não pode remover a si mesmo por este método
        if(owner.getId().equals(barberToRemove.getId())) {
            throw new ReferenceException("O dono não pode remover a si mesmo.");
        }

        // 4. Desvincula o barbeiro
        barberToRemove.setBarbershop(null);
        barberToRemove.getActivities().clear(); // Limpa os vínculos de serviço
        barberRepository.save(barberToRemove);
    }
}
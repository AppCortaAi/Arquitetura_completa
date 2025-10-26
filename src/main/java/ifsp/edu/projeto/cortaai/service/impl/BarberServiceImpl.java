package ifsp.edu.projeto.cortaai.service.impl;

import ifsp.edu.projeto.cortaai.dto.*;
import ifsp.edu.projeto.cortaai.events.BeforeDeleteBarber;
import ifsp.edu.projeto.cortaai.exception.NotFoundException;
import ifsp.edu.projeto.cortaai.exception.ReferenceException; // Usado para regras de negócio
import ifsp.edu.projeto.cortaai.mapper.BarberMapper;
import ifsp.edu.projeto.cortaai.model.Barber;
import ifsp.edu.projeto.cortaai.model.Barbershop;
import ifsp.edu.projeto.cortaai.model.BarbershopJoinRequest;
import ifsp.edu.projeto.cortaai.model.enums.JoinRequestStatus;
import ifsp.edu.projeto.cortaai.repository.*;
import ifsp.edu.projeto.cortaai.service.BarberService;
import ifsp.edu.projeto.cortaai.model.Activity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BarberServiceImpl implements BarberService {

    private final BarberRepository barberRepository;
    private final BarbershopRepository barbershopRepository;
    private final BarbershopJoinRequestRepository joinRequestRepository;
    private final ServiceRepository serviceRepository;
    private final ApplicationEventPublisher publisher;
    private final BarberMapper barberMapper;
    private final PasswordEncoder passwordEncoder;

    // Mappers que serão criados na próxima etapa
    // private final BarbershopMapper barbershopMapper;
    // private final ServiceMapper serviceMapper;

    public BarberServiceImpl(final BarberRepository barberRepository,
                             final BarbershopRepository barbershopRepository,
                             final BarbershopJoinRequestRepository joinRequestRepository,
                             final ServiceRepository serviceRepository,
                             final ApplicationEventPublisher publisher,
                             final BarberMapper barberMapper,
                             final PasswordEncoder passwordEncoder) {
        this.barberRepository = barberRepository;
        this.barbershopRepository = barbershopRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.serviceRepository = serviceRepository;
        this.publisher = publisher;
        this.barberMapper = barberMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // --- Gestão de Barbeiros (Global) ---

    @Override
    public List<BarberDTO> findAll() {
        return barberRepository.findAll(Sort.by("id")).stream()
                .map(barberMapper::toDTO)
                .toList();
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
                .orElseThrow(() -> new NotFoundException("Barbeiro (Dono) não encontrado"));

        if (owner.getBarbershop() != null) {
            throw new ReferenceException("Barbeiro já está vinculado a uma barbearia.");
        }

        final Barbershop barbershop = new Barbershop();
        barbershop.setName(createBarbershopDTO.getName());
        barbershop.setCnpj(createBarbershopDTO.getCnpj());
        barbershop.setAddress(createBarbershopDTO.getAddress());
        final Barbershop savedBarbershop = barbershopRepository.save(barbershop);

        // Vincula o barbeiro como dono
        owner.setBarbershop(savedBarbershop);
        owner.setOwner(true);
        barberRepository.save(owner);

        // TODO: Retornar DTO usando o BarbershopMapper
        // return barbershopMapper.toDTO(savedBarbershop);
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
    public ActivityDTO createService(final UUID ownerBarberId, final CreateActivityDTO createActivityDTO) {
        final Barber owner = barberRepository.findById(ownerBarberId)
                .orElseThrow(() -> new NotFoundException("Barbeiro (Dono) não encontrado"));

        if (!owner.isOwner() || owner.getBarbershop() == null) {
            throw new ReferenceException("Apenas o dono da barbearia pode criar serviços.");
        }

        final Activity service = new Activity();
        service.setBarbershop(owner.getBarbershop());
        service.setActivityName(createActivityDTO.getActivityName());
        service.setPrice(createActivityDTO.getPrice());
        service.setDurationMinutes(createActivityDTO.getDurationMinutes());
        final Activity savedService = serviceRepository.save(service);

        // TODO: Retornar DTO usando o ServiceMapper
        // return serviceMapper.toDTO(savedService);
        throw new UnsupportedOperationException("ServiceMapper ainda não implementado");
    }

    @Override
    public List<ActivityDTO> listServices(final UUID barbershopId) {
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
    public void leaveBarbershop(final UUID barberId) {
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

    // --- Gestão de Habilidades (Fluxo 2) ---

    @Override
    @Transactional
    public void assignServices(final UUID barberId, final BarberServiceAssignDTO barberServiceAssignDTO) {
        final Barber barber = barberRepository.findById(barberId)
                .orElseThrow(NotFoundException::new);

        if (barber.getBarbershop() == null) {
            throw new ReferenceException("Barbeiro não está em uma barbearia para vincular serviços.");
        }

        final UUID barbershopId = barber.getBarbershop().getId();

        // Busca os serviços e valida se pertencem à loja correta
        final Set<Activity> servicesToAssign = new HashSet<>(
                serviceRepository.findAllById(barberServiceAssignDTO.getActivityIds())
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
}
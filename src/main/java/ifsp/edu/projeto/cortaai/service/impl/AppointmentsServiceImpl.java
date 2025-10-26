package ifsp.edu.projeto.cortaai.service.impl;

import ifsp.edu.projeto.cortaai.dto.AppointmentRequestDTO;
import ifsp.edu.projeto.cortaai.dto.AppointmentsDTO;
import ifsp.edu.projeto.cortaai.exception.NotFoundException;
import ifsp.edu.projeto.cortaai.exception.ReferenceException;
import ifsp.edu.projeto.cortaai.mapper.AppointmentMapper;
import ifsp.edu.projeto.cortaai.model.*;
import ifsp.edu.projeto.cortaai.model.enums.AppointmentStatus;
import ifsp.edu.projeto.cortaai.repository.*;
import ifsp.edu.projeto.cortaai.service.AppointmentsService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppointmentsServiceImpl implements AppointmentsService {

    private final AppointmentsRepository appointmentsRepository;
    private final BarberRepository barberRepository;
    private final CustomerRepository customerRepository;
    private final BarbershopRepository barbershopRepository;
    private final ActivityRepository activityRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentsServiceImpl(final AppointmentsRepository appointmentsRepository,
                                   final BarberRepository barberRepository,
                                   final CustomerRepository customerRepository,
                                   final BarbershopRepository barbershopRepository,
                                   final ActivityRepository activityRepository,
                                   final AppointmentMapper appointmentMapper) {
        this.appointmentsRepository = appointmentsRepository;
        this.barberRepository = barberRepository;
        this.customerRepository = customerRepository;
        this.barbershopRepository = barbershopRepository;
        this.activityRepository = activityRepository;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    public List<AppointmentsDTO> findAll() {
        final List<Appointments> appointmentsList = appointmentsRepository.findAll(Sort.by("id"));
        return appointmentsList.stream()
                .map(appointmentMapper::toDTO)
                .toList();
    }

    @Override
    public AppointmentsDTO get(final Long id) {
        return appointmentsRepository.findById(id)
                .map(appointmentMapper::toDTO)
                .orElseThrow(NotFoundException::new);
    }

    @Override
    @Transactional
    public Long create(final AppointmentRequestDTO appointmentsDTO) {
        // 1. Validar e buscar entidades
        final Barbershop barbershop = barbershopRepository.findById(appointmentsDTO.getBarbershopId())
                .orElseThrow(() -> new NotFoundException("Barbearia não encontrada"));

        final Customer customer = customerRepository.findById(appointmentsDTO.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        final Barber barber = barberRepository.findById(appointmentsDTO.getBarberId())
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado"));

        // 2. Validar se o barbeiro pertence à barbearia
        if (barber.getBarbershop() == null || !barber.getBarbershop().getId().equals(barbershop.getId())) {
            throw new ReferenceException();
        }

        // 3. Validar serviços e calcular tempo
        final Set<Activity> activities = new HashSet<>(
                activityRepository.findAllById(appointmentsDTO.getActivityIds())
        );

        if(activities.size() != appointmentsDTO.getActivityIds().size()) {
            throw new NotFoundException("Um ou mais serviços não foram encontrados.");
        }

        int totalDuration = 0;
        for (Activity s : activities) {
            // 3a. Valida se o serviço é da barbearia
            if (!s.getBarbershop().getId().equals(barbershop.getId())) {
                throw new ReferenceException("Serviço " + s.getActivityName() + " não pertence a esta barbearia.");
            }
            // 3b. Valida se o barbeiro executa o serviço
            if (!barber.getActivities().contains(s)) {
                throw new ReferenceException("Barbeiro " + barber.getName() + " não executa o serviço " + s.getActivityName() + ".");
            }
            totalDuration += s.getDurationMinutes();
        }

        // 4. Calcular horário e verificar conflitos
        final OffsetDateTime startTime = appointmentsDTO.getStartTime();
        final OffsetDateTime endTime = startTime.plusMinutes(totalDuration);

        List<Appointments> conflicts = appointmentsRepository.findConflictingAppointments(
                barber.getId(), startTime, endTime
        );

        if (!conflicts.isEmpty()) {
            throw new ReferenceException("Horário indisponível. Já existe um agendamento neste bloco.");
        }

        // 5. Criar e salvar
        final Appointments appointments = new Appointments();
        appointments.setBarbershop(barbershop);
        appointments.setBarber(barber);
        appointments.setCustomer(customer);
        appointments.setStartTime(startTime);
        appointments.setEndTime(endTime);
        appointments.setStatus(AppointmentStatus.SCHEDULED);
        appointments.setActivities(activities);

        return appointmentsRepository.save(appointments).getId();
    }

    @Override
    @Transactional
    public void update(final Long id, final AppointmentRequestDTO appointmentsDTO) {
        final Appointments appointments = appointmentsRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        // Valida se o agendamento pode ser alterado
        if (appointments.getStatus() == AppointmentStatus.CONCLUDED || appointments.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ReferenceException("Agendamentos concluídos ou cancelados não podem ser alterados.");
        }

        // (Lógica de validação e cálculo idêntica ao CREATE)

        final Barbershop barbershop = barbershopRepository.findById(appointmentsDTO.getBarbershopId())
                .orElseThrow(() -> new NotFoundException("Barbearia não encontrada"));

        final Customer customer = customerRepository.findById(appointmentsDTO.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        final Barber barber = barberRepository.findById(appointmentsDTO.getBarberId())
                .orElseThrow(() -> new NotFoundException("Barbeiro não encontrado"));

        if (barber.getBarbershop() == null || !barber.getBarbershop().getId().equals(barbershop.getId())) {
            throw new ReferenceException("O barbeiro selecionado não pertence a esta barbearia.");
        }

        final Set<Activity> activities = new HashSet<>(
                activityRepository.findAllById(appointmentsDTO.getActivityIds())
        );

        int totalDuration = 0;
        for (Activity s : activities) {
            if (!s.getBarbershop().getId().equals(barbershop.getId())) {
                throw new ReferenceException("Serviço " + s.getActivityName() + " não pertence a esta barbearia.");
            }
            if (!barber.getActivities().contains(s)) {
                throw new ReferenceException("Barbeiro " + barber.getName() + " não executa o serviço " + s.getActivityName() + ".");
            }
            totalDuration += s.getDurationMinutes();
        }

        final OffsetDateTime startTime = appointmentsDTO.getStartTime();
        final OffsetDateTime endTime = startTime.plusMinutes(totalDuration);

        // No update, temos que ignorar o próprio agendamento na verificação de conflito
        List<Appointments> conflicts = appointmentsRepository.findConflictingAppointments(
                barber.getId(), startTime, endTime
        ).stream().filter(a -> !a.getId().equals(id)).collect(Collectors.toList()); // Exclui o próprio ID da verificação

        if (!conflicts.isEmpty()) {
            throw new ReferenceException("Horário indisponível. Já existe um agendamento neste bloco.");
        }

        // Atualiza a entidade existente
        appointments.setBarbershop(barbershop);
        appointments.setBarber(barber);
        appointments.setCustomer(customer);
        appointments.setStartTime(startTime);
        appointments.setEndTime(endTime);
        appointments.setStatus(AppointmentStatus.SCHEDULED); // Re-agenda
        appointments.setActivities(activities);

        appointmentsRepository.save(appointments);
    }

    @Override
    @Transactional
    public void cancel(final Long id) {
        final Appointments appointments = appointmentsRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        if (appointments.getStatus() == AppointmentStatus.CONCLUDED) {
            throw new ReferenceException("Agendamentos concluídos não podem ser cancelados.");
        }

        appointments.setStatus(AppointmentStatus.CANCELLED);
        appointmentsRepository.save(appointments);
    }

    @Override
    public void delete(final Long id) {
        if (!appointmentsRepository.existsById(id)) {
            throw new NotFoundException();
        }
        // A regra de negócio sugere usar 'cancel' ao invés de delete físico
        // para manter histórico.
        appointmentsRepository.deleteById(id);
    }
}
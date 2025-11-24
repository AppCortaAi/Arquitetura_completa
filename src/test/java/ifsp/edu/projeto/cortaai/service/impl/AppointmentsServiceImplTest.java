package ifsp.edu.projeto.cortaai.service.impl;

import ifsp.edu.projeto.cortaai.dto.AppointmentRequestDTO;
import ifsp.edu.projeto.cortaai.dto.AppointmentsDTO;
import ifsp.edu.projeto.cortaai.exception.NotFoundException;
import ifsp.edu.projeto.cortaai.exception.ReferenceException;
import ifsp.edu.projeto.cortaai.mapper.AppointmentMapper;
import ifsp.edu.projeto.cortaai.model.*;
import ifsp.edu.projeto.cortaai.model.enums.AppointmentStatus;
import ifsp.edu.projeto.cortaai.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AppointmentsServiceImpl")
class AppointmentsServiceImplTest {

    @Mock
    private AppointmentsRepository appointmentsRepository;
    @Mock
    private BarberRepository barberRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private BarbershopRepository barbershopRepository;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentsServiceImpl appointmentsService;

    // Entidades de Mock
    private Customer mockCustomer;
    private Barber mockBarber;
    private Barber mockOwner;
    private Barbershop mockBarbershop;
    private Activity mockActivity;
    private Appointments mockAppointment;
    private AppointmentRequestDTO requestDTO;

    // Dados de Mock
    private final String customerEmail = "cliente@teste.com";
    private final String barberEmail = "barbeiro@teste.com";
    private final String ownerEmail = "dono@teste.com";
    private final UUID customerId = UUID.randomUUID();
    private final UUID barberId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();
    private final UUID barbershopId = UUID.randomUUID();
    private final UUID activityId = UUID.randomUUID();
    private final Long appointmentId = 1L;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    @BeforeEach
    void setUp() {
        // Mocks de Entidades
        mockBarbershop = new Barbershop();
        mockBarbershop.setId(barbershopId);
        mockBarbershop.setName("Barbearia Teste");

        mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setEmail(customerEmail);

        mockBarber = new Barber();
        mockBarber.setId(barberId);
        mockBarber.setEmail(barberEmail);
        mockBarber.setBarbershop(mockBarbershop); // Barbeiro pertence à barbearia
        mockBarber.setWorkStartTime(LocalTime.of(9, 0));
        mockBarber.setWorkEndTime(LocalTime.of(18, 0));

        mockOwner = new Barber();
        mockOwner.setId(ownerId);
        mockOwner.setEmail(ownerEmail);
        mockOwner.setOwner(true);
        mockOwner.setBarbershop(mockBarbershop); // Dono é dono da barbearia

        mockActivity = new Activity();
        mockActivity.setId(activityId);
        mockActivity.setBarbershop(mockBarbershop); // Atividade pertence à barbearia
        mockActivity.setDurationMinutes(30);

        // Barbeiro pode realizar a atividade
        mockBarber.setActivities(Set.of(mockActivity));

        // DTO de Requisição
        startTime = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).withHour(10).withMinute(0).withNano(0);
        endTime = startTime.plusMinutes(mockActivity.getDurationMinutes());

        requestDTO = new AppointmentRequestDTO();
        requestDTO.setBarbershopId(barbershopId);
        requestDTO.setBarberId(barberId);
        requestDTO.setActivityIds(List.of(activityId));
        requestDTO.setStartTime(startTime);

        // Agendamento
        mockAppointment = new Appointments();
        mockAppointment.setId(appointmentId);
        mockAppointment.setCustomer(mockCustomer);
        mockAppointment.setBarber(mockBarber);
        mockAppointment.setBarbershop(mockBarbershop);
        mockAppointment.setStatus(AppointmentStatus.SCHEDULED);
        mockAppointment.setStartTime(startTime);
        mockAppointment.setEndTime(endTime);
    }

    // --- Métodos de Busca (find...) ---

    @Test
    @DisplayName("findForBarber: Deve retornar lista DTO do barbeiro")
    void findForBarber_ShouldReturnDtoList_WhenBarberExists() {
        // Arrange
        when(barberRepository.findByEmail(barberEmail)).thenReturn(Optional.of(mockBarber));
        when(appointmentsRepository.findByBarberId(barberId)).thenReturn(List.of(mockAppointment));
        when(appointmentMapper.toDTO(any(Appointments.class))).thenReturn(new AppointmentsDTO());

        // Act
        List<AppointmentsDTO> result = appointmentsService.findForBarber(barberEmail);

        // Assert
        assertFalse(result.isEmpty());
        verify(barberRepository, times(1)).findByEmail(barberEmail);
        verify(appointmentsRepository, times(1)).findByBarberId(barberId);
    }

    @Test
    @DisplayName("findForBarber: Deve lançar NotFoundException se barbeiro não existe")
    void findForBarber_ShouldThrowNotFound_WhenBarberNotExists() {
        // Arrange
        when(barberRepository.findByEmail(barberEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            appointmentsService.findForBarber(barberEmail);
        });
        verify(appointmentsRepository, never()).findByBarberId(any());
    }

    @Test
    @DisplayName("findForBarbershop: Deve retornar lista DTO do dono")
    void findForBarbershop_ShouldReturnDtoList_WhenUserIsOwner() {
        // Arrange
        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner));
        when(appointmentsRepository.findByBarbershopId(barbershopId)).thenReturn(List.of(mockAppointment));
        when(appointmentMapper.toDTO(any(Appointments.class))).thenReturn(new AppointmentsDTO());

        // Act
        List<AppointmentsDTO> result = appointmentsService.findForBarbershop(ownerEmail);

        // Assert
        assertFalse(result.isEmpty());
        verify(barberRepository, times(1)).findByEmail(ownerEmail);
        verify(appointmentsRepository, times(1)).findByBarbershopId(barbershopId);
    }

    @Test
    @DisplayName("findForBarbershop: Deve lançar ReferenceException se usuário não for dono")
    void findForBarbershop_ShouldThrowReferenceException_WhenUserIsNotOwner() {
        // Arrange
        mockBarber.setOwner(false); // Garante que não é dono
        when(barberRepository.findByEmail(barberEmail)).thenReturn(Optional.of(mockBarber));

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            appointmentsService.findForBarbershop(barberEmail);
        });
        verify(appointmentsRepository, never()).findByBarbershopId(any());
    }

    @Test
    @DisplayName("findForCustomer: Deve retornar lista DTO do cliente")
    void findForCustomer_ShouldReturnDtoList_WhenCustomerExists() {
        // Arrange
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(appointmentsRepository.findByCustomerId(customerId)).thenReturn(List.of(mockAppointment));
        when(appointmentMapper.toDTO(any(Appointments.class))).thenReturn(new AppointmentsDTO());

        // Act
        List<AppointmentsDTO> result = appointmentsService.findForCustomer(customerEmail);

        // Assert
        assertFalse(result.isEmpty());
        verify(customerRepository, times(1)).findByEmail(customerEmail);
        verify(appointmentsRepository, times(1)).findByCustomerId(customerId);
    }

    // --- Testes de create() ---

    @Test
    @DisplayName("create: Deve criar agendamento com sucesso")
    void create_ShouldSucceed_WhenAllDataIsValid() {
        // Arrange
        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(mockBarbershop));
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(mockBarber));
        when(activityRepository.findAllById(requestDTO.getActivityIds())).thenReturn(List.of(mockActivity));
        when(appointmentsRepository.findConflictingAppointments(barberId, startTime, endTime)).thenReturn(Collections.emptyList());
        when(appointmentsRepository.findConflictingAppointmentsForCustomer(customerId, startTime, endTime)).thenReturn(Collections.emptyList());
        when(appointmentsRepository.save(any(Appointments.class))).thenReturn(mockAppointment);

        // Act
        Long resultId = appointmentsService.create(requestDTO, customerEmail);

        // Assert
        assertEquals(appointmentId, resultId);
        verify(appointmentsRepository, times(1)).save(any(Appointments.class));
    }

    @Test
    @DisplayName("create: Deve lançar ReferenceException se barbeiro não pertencer à barbearia")
    void create_ShouldThrowReferenceException_WhenBarberNotAtBarbershop() {
        // Arrange
        Barbershop outraBarbearia = new Barbershop();
        outraBarbearia.setId(UUID.randomUUID()); // Garante que a outra barbearia tenha um ID diferente
        mockBarber.setBarbershop(outraBarbearia); // Barbeiro em outra loja
        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(mockBarbershop));
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(mockBarber));

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            appointmentsService.create(requestDTO, customerEmail);
        });
    }

    @Test
    @DisplayName("create: Deve lançar ReferenceException se atividade não pertencer à barbearia")
    void create_ShouldThrowReferenceException_WhenActivityNotAtBarbershop() {
        // Arrange
        Barbershop outraBarbearia = new Barbershop();
        outraBarbearia.setId(UUID.randomUUID()); // Garante que a outra barbearia tenha um ID diferente
        mockActivity.setBarbershop(outraBarbearia); // Atividade de outra loja
        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(mockBarbershop));
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(mockBarber));
        when(activityRepository.findAllById(requestDTO.getActivityIds())).thenReturn(List.of(mockActivity));

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            appointmentsService.create(requestDTO, customerEmail);
        });
    }

    @Test
    @DisplayName("create: Deve lançar ReferenceException se barbeiro não realizar atividade")
    void create_ShouldThrowReferenceException_WhenBarberCannotDoActivity() {
        // Arrange
        mockBarber.setActivities(Collections.emptySet()); // Barbeiro não sabe fazer nada
        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(mockBarbershop));
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(mockBarber));
        when(activityRepository.findAllById(requestDTO.getActivityIds())).thenReturn(List.of(mockActivity));

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            appointmentsService.create(requestDTO, customerEmail);
        });
    }

    @Test
    @DisplayName("create: Deve lançar ReferenceException se fora do expediente")
    void create_ShouldThrowReferenceException_WhenOutsideWorkHours() {
        // Arrange
        requestDTO.setStartTime(startTime.withHour(8)); // 8h, antes das 9h
        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(mockBarbershop));
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(mockBarber));
        when(activityRepository.findAllById(requestDTO.getActivityIds())).thenReturn(List.of(mockActivity));

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            appointmentsService.create(requestDTO, customerEmail);
        });
    }

    @Test
    @DisplayName("create: Deve lançar ReferenceException se houver conflito do barbeiro")
    void create_ShouldThrowReferenceException_WhenBarberHasConflict() {
        // Arrange
        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(mockBarbershop));
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(mockBarber));
        when(activityRepository.findAllById(requestDTO.getActivityIds())).thenReturn(List.of(mockActivity));
        // Conflito encontrado!
        when(appointmentsRepository.findConflictingAppointments(barberId, startTime, endTime)).thenReturn(List.of(new Appointments()));

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            appointmentsService.create(requestDTO, customerEmail);
        });
    }

    // --- Testes de update() ---

    @Test
    @DisplayName("update: Deve atualizar agendamento com sucesso")
    void update_ShouldSucceed_WhenAllDataIsValid() {
        // Arrange
        // Mocks para carregar o agendamento e validar o usuário
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        // Mocks para validar a lógica de atualização (igual ao create)
        when(barbershopRepository.findById(barbershopId)).thenReturn(Optional.of(mockBarbershop));
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(mockBarber));
        when(activityRepository.findAllById(requestDTO.getActivityIds())).thenReturn(List.of(mockActivity));
        // Conflito (ignorando o próprio ID)
        when(appointmentsRepository.findConflictingAppointments(barberId, startTime, endTime)).thenReturn(Collections.emptyList());
        when(appointmentsRepository.save(any(Appointments.class))).thenReturn(mockAppointment);

        // Act
        appointmentsService.update(appointmentId, requestDTO, customerEmail);

        // Assert
        verify(appointmentsRepository, times(1)).save(mockAppointment);
        assertEquals(AppointmentStatus.SCHEDULED, mockAppointment.getStatus()); // Garante que foi resetado para SCHEDULED
    }

    @Test
    @DisplayName("update: Deve lançar ReferenceException se usuário não for o dono do agendamento")
    void update_ShouldThrowReferenceException_WhenUserIsNotCustomer() {
        // Arrange
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(new Customer())); // Outro cliente

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            appointmentsService.update(appointmentId, requestDTO, "outro@email.com");
        });
    }

    @Test
    @DisplayName("update: Deve lançar ReferenceException se agendamento estiver CONCLUDED")
    void update_ShouldThrowReferenceException_WhenAppointmentIsConcluded() {
        // Arrange
        mockAppointment.setStatus(AppointmentStatus.CONCLUDED);
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            appointmentsService.update(appointmentId, requestDTO, customerEmail);
        });
    }

    // --- Testes de conclude(), cancel() e delete() ---

    @Test
    @DisplayName("conclude: Deve concluir agendamento se for o barbeiro correto")
    void conclude_ShouldSucceed_WhenUserIsAssignedBarber() {
        // Arrange
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        when(barberRepository.findByEmail(barberEmail)).thenReturn(Optional.of(mockBarber));

        // Act
        appointmentsService.conclude(appointmentId, barberEmail);

        // Assert
        assertEquals(AppointmentStatus.CONCLUDED, mockAppointment.getStatus());
        verify(appointmentsRepository, times(1)).save(mockAppointment);
    }

    @Test
    @DisplayName("conclude: Deve lançar ReferenceException se não for o barbeiro do agendamento")
    void conclude_ShouldThrowReferenceException_WhenUserIsNotAssignedBarber() {
        // Arrange
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner)); // Outro barbeiro (dono)

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            appointmentsService.conclude(appointmentId, ownerEmail);
        });
    }

    @Test
    @DisplayName("cancel: Deve cancelar se for o Cliente")
    void cancel_ShouldSucceed_WhenUserIsCustomer() {
        // Arrange
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(barberRepository.findByEmail(customerEmail)).thenReturn(Optional.empty()); // Não é barbeiro

        // Act
        appointmentsService.cancel(appointmentId, customerEmail);

        // Assert
        assertEquals(AppointmentStatus.CANCELLED, mockAppointment.getStatus());
    }

    @Test
    @DisplayName("cancel: Deve cancelar se for o Dono da Barbearia")
    void cancel_ShouldSucceed_WhenUserIsOwner() {
        // Arrange
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        when(customerRepository.findByEmail(ownerEmail)).thenReturn(Optional.empty());
        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner));

        // Act
        appointmentsService.cancel(appointmentId, ownerEmail);

        // Assert
        assertEquals(AppointmentStatus.CANCELLED, mockAppointment.getStatus());
    }

    @Test
    @DisplayName("cancel: Deve cancelar se for o Barbeiro do Agendamento")
    void cancel_ShouldSucceed_WhenUserIsAssignedBarber() {
        // Arrange
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        when(customerRepository.findByEmail(barberEmail)).thenReturn(Optional.empty());
        when(barberRepository.findByEmail(barberEmail)).thenReturn(Optional.of(mockBarber));

        // Act
        appointmentsService.cancel(appointmentId, barberEmail);

        // Assert
        assertEquals(AppointmentStatus.CANCELLED, mockAppointment.getStatus());
    }

    @Test
    @DisplayName("cancel: Deve lançar ReferenceException se usuário não tiver permissão")
    void cancel_ShouldThrowReferenceException_WhenUserIsUnauthorized() {
        // Arrange
        Barber outroBarbeiro = new Barber();
        outroBarbeiro.setId(UUID.randomUUID());
        outroBarbeiro.setBarbershop(mockBarbershop); // É da loja, mas não é o dono nem o barbeiro do agendamento

        String outroEmail = "outro@barbeiro.com";
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        when(customerRepository.findByEmail(outroEmail)).thenReturn(Optional.empty());
        when(barberRepository.findByEmail(outroEmail)).thenReturn(Optional.of(outroBarbeiro));

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            appointmentsService.cancel(appointmentId, outroEmail);
        });
    }

    @Test
    @DisplayName("delete: Deve deletar se for o Dono")
    void delete_ShouldSucceed_WhenUserIsOwner() {
        // Arrange
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner));

        // Act
        appointmentsService.delete(appointmentId, ownerEmail);

        // Assert
        verify(appointmentsRepository, times(1)).deleteById(appointmentId);
    }

    @Test
    @DisplayName("delete: Deve lançar ReferenceException se não for o Dono")
    void delete_ShouldThrowReferenceException_WhenUserIsNotOwner() {
        // Arrange
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));
        when(barberRepository.findByEmail(barberEmail)).thenReturn(Optional.of(mockBarber)); // Barbeiro (não-dono)

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            appointmentsService.delete(appointmentId, barberEmail);
        });
    }
}
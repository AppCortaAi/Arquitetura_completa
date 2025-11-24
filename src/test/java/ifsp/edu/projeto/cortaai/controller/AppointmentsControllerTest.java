package ifsp.edu.projeto.cortaai.controller;

import ifsp.edu.projeto.cortaai.dto.AppointmentRequestDTO;
import ifsp.edu.projeto.cortaai.dto.AppointmentsDTO;
import ifsp.edu.projeto.cortaai.service.AppointmentsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AppointmentsController")
class AppointmentsControllerTest {

    @Mock
    private AppointmentsService appointmentsService; // Dependência mocada

    @InjectMocks
    private AppointmentsController appointmentsController; // Classe sob teste

    @Mock
    private Principal mockPrincipal; // Mock para simular usuário logado

    private AppointmentsDTO appointmentsDTO;
    private AppointmentRequestDTO appointmentRequestDTO;
    private final String testEmail = "usuario@teste.com";
    private final Long testId = 1L;

    @BeforeEach
    void setUp() {
        // Configura um DTO padrão para retorno
        appointmentsDTO = new AppointmentsDTO();
        appointmentsDTO.setId(testId);
        appointmentsDTO.setBarberId(UUID.randomUUID());
        appointmentsDTO.setCustomerId(UUID.randomUUID());

        // Configura um DTO de requisição
        appointmentRequestDTO = new AppointmentRequestDTO();
        appointmentRequestDTO.setBarberId(UUID.randomUUID());
        appointmentRequestDTO.setStartTime(OffsetDateTime.now().plusDays(1));
    }

    @Test
    @DisplayName("Deve retornar lista de DTOs e status OK 200")
    void getAllAppointments_ShouldReturnDtoList_And_StatusOk() {
        // Arrange
        when(appointmentsService.findAll()).thenReturn(List.of(appointmentsDTO));

        // Act
        ResponseEntity<List<AppointmentsDTO>> response = appointmentsController.getAllAppointments();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(appointmentsDTO.getId(), response.getBody().get(0).getId());
        verify(appointmentsService, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar um DTO quando ID existe e status OK 200")
    void getAppointments_ShouldReturnDto_And_StatusOk() {
        // Arrange
        when(appointmentsService.get(testId)).thenReturn(appointmentsDTO);

        // Act
        ResponseEntity<AppointmentsDTO> response = appointmentsController.getAppointments(testId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(appointmentsDTO.getId(), response.getBody().getId());
        verify(appointmentsService, times(1)).get(testId);
    }

    @Test
    @DisplayName("Deve retornar agendamentos da barbearia (dono) e status OK 200")
    void getAppointmentsForBarbershop_ShouldReturnList_And_StatusOk() {
        // Arrange
        when(mockPrincipal.getName()).thenReturn(testEmail);
        when(appointmentsService.findForBarbershop(testEmail)).thenReturn(List.of(appointmentsDTO));

        // Act
        ResponseEntity<List<AppointmentsDTO>> response = appointmentsController.getAppointmentsForBarbershop(mockPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appointmentsService, times(1)).findForBarbershop(testEmail);
    }

    @Test
    @DisplayName("Deve retornar agendamentos do barbeiro (logado) e status OK 200")
    void getAppointmentsForBarber_ShouldReturnList_And_StatusOk() {
        // Arrange
        when(mockPrincipal.getName()).thenReturn(testEmail);
        when(appointmentsService.findForBarber(testEmail)).thenReturn(List.of(appointmentsDTO));

        // Act
        ResponseEntity<List<AppointmentsDTO>> response = appointmentsController.getAppointmentsForBarber(mockPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appointmentsService, times(1)).findForBarber(testEmail);
    }

    @Test
    @DisplayName("Deve retornar agendamentos do cliente (logado) e status OK 200")
    void getAppointmentsForCustomer_ShouldReturnList_And_StatusOk() {
        // Arrange
        when(mockPrincipal.getName()).thenReturn(testEmail);
        when(appointmentsService.findForCustomer(testEmail)).thenReturn(List.of(appointmentsDTO));

        // Act
        ResponseEntity<List<AppointmentsDTO>> response = appointmentsController.getAppointmentsForCustomer(mockPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(appointmentsService, times(1)).findForCustomer(testEmail);
    }

    @Test
    @DisplayName("Deve criar agendamento e retornar ID com status CREATED 201")
    void createAppointments_ShouldReturnCreatedId_And_StatusCreated() {
        // Arrange
        when(mockPrincipal.getName()).thenReturn(testEmail);
        when(appointmentsService.create(any(AppointmentRequestDTO.class), eq(testEmail))).thenReturn(testId);

        // Act
        ResponseEntity<Long> response = appointmentsController.createAppointments(appointmentRequestDTO, mockPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testId, response.getBody());
        verify(appointmentsService, times(1)).create(appointmentRequestDTO, testEmail);
    }

    @Test
    @DisplayName("Deve atualizar agendamento e retornar ID com status OK 200")
    void updateAppointments_ShouldReturnId_And_StatusOk() {
        // Arrange
        when(mockPrincipal.getName()).thenReturn(testEmail);
        doNothing().when(appointmentsService).update(eq(testId), any(AppointmentRequestDTO.class), eq(testEmail));

        // Act
        ResponseEntity<Long> response = appointmentsController.updateAppointments(testId, appointmentRequestDTO, mockPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testId, response.getBody());
        verify(appointmentsService, times(1)).update(testId, appointmentRequestDTO, testEmail);
    }

    @Test
    @DisplayName("Deve cancelar agendamento e retornar status NO_CONTENT 204")
    void cancelAppointments_ShouldReturn_StatusNoContent() {
        // Arrange
        when(mockPrincipal.getName()).thenReturn(testEmail);
        doNothing().when(appointmentsService).cancel(eq(testId), eq(testEmail));

        // Act
        ResponseEntity<Void> response = appointmentsController.cancelAppointments(testId, mockPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appointmentsService, times(1)).cancel(testId, testEmail);
    }

    @Test
    @DisplayName("Deve concluir agendamento e retornar status NO_CONTENT 204")
    void concludeAppointment_ShouldReturn_StatusNoContent() {
        // Arrange
        when(mockPrincipal.getName()).thenReturn(testEmail);
        doNothing().when(appointmentsService).conclude(eq(testId), eq(testEmail));

        // Act
        ResponseEntity<Void> response = appointmentsController.concludeAppointment(testId, mockPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appointmentsService, times(1)).conclude(testId, testEmail);
    }

    @Test
    @DisplayName("Deve deletar agendamento e retornar status NO_CONTENT 204")
    void deleteAppointments_ShouldReturn_StatusNoContent() {
        // Arrange
        when(mockPrincipal.getName()).thenReturn(testEmail);
        doNothing().when(appointmentsService).delete(eq(testId), eq(testEmail));

        // Act
        ResponseEntity<Void> response = appointmentsController.deleteAppointments(testId, mockPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appointmentsService, times(1)).delete(testId, testEmail);
    }
}
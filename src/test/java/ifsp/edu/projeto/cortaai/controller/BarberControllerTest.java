package ifsp.edu.projeto.cortaai.controller;

import ifsp.edu.projeto.cortaai.dto.*;
import ifsp.edu.projeto.cortaai.service.BarberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para BarberController")
class BarberControllerTest {

    @Mock
    private BarberService barberService; // Dependência mocada

    @InjectMocks
    private BarberController barberController; // Classe sob teste

    @Mock
    private Principal mockPrincipal; // Mock para simular usuário logado

    private BarberDTO barberDTO;
    private CreateBarberDTO createBarberDTO;
    private LoginDTO loginDTO;
    private LoginResponseDTO loginResponseDTO;
    private MockMultipartFile mockFile;
    private final UUID testUuid = UUID.randomUUID();
    private final String testEmail = "barbeiro@teste.com";

    @BeforeEach
    void setUp() {
        barberDTO = new BarberDTO();
        barberDTO.setId(testUuid);
        barberDTO.setEmail(testEmail);
        barberDTO.setName("Barbeiro Teste");

        createBarberDTO = new CreateBarberDTO();
        createBarberDTO.setEmail(testEmail);
        createBarberDTO.setPassword("123456");

        loginDTO = new LoginDTO();
        loginDTO.setEmail(testEmail);
        loginDTO.setPassword("123456");

        loginResponseDTO = LoginResponseDTO.builder()
                .token("dummy.token.123")
                .userData(barberDTO)
                .build();

        mockFile = new MockMultipartFile(
                "file",
                "foto.jpg",
                "image/jpeg",
                "some-image-bytes".getBytes()
        );

        // Configuração padrão para o mock do Principal
        lenient().when(mockPrincipal.getName()).thenReturn(testEmail);
    }

    @Test
    @DisplayName("Deve retornar lista de DTOs de Barbeiros e status OK 200")
    void getAllBarbers_ShouldReturnDtoList_And_StatusOk() {
        // Arrange
        when(barberService.findAll()).thenReturn(List.of(barberDTO));

        // Act
        ResponseEntity<List<BarberDTO>> response = barberController.getAllBarbers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(barberService, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar um DTO de Barbeiro quando ID existe e status OK 200")
    void getBarber_ShouldReturnDto_And_StatusOk() {
        // Arrange
        when(barberService.get(testUuid)).thenReturn(barberDTO);

        // Act
        ResponseEntity<BarberDTO> response = barberController.getBarber(testUuid);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(barberDTO.getId(), response.getBody().getId());
        verify(barberService, times(1)).get(testUuid);
    }

    @Test
    @DisplayName("Deve criar barbeiro e retornar UUID com status CREATED 201 (com arquivo)")
    void createBarber_WithFile_ShouldReturnCreatedId_And_StatusCreated() throws IOException {
        // Arrange
        when(barberService.create(any(CreateBarberDTO.class), any(MultipartFile.class))).thenReturn(testUuid);

        // Act
        ResponseEntity<UUID> response = barberController.createBarber(createBarberDTO, mockFile);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testUuid, response.getBody());
        verify(barberService, times(1)).create(createBarberDTO, mockFile);
    }

    @Test
    @DisplayName("Deve criar barbeiro e retornar UUID com status CREATED 201 (sem arquivo)")
    void createBarber_WithoutFile_ShouldReturnCreatedId_And_StatusCreated() throws IOException {
        // Arrange
        when(barberService.create(any(CreateBarberDTO.class), isNull())).thenReturn(testUuid);

        // Act
        ResponseEntity<UUID> response = barberController.createBarber(createBarberDTO, null);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testUuid, response.getBody());
        verify(barberService, times(1)).create(createBarberDTO, null);
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR 500 ao falhar no upload do registro")
    void createBarber_WhenUploadFails_ShouldReturnStatus500() throws IOException {
        // Arrange
        when(barberService.create(any(CreateBarberDTO.class), any(MultipartFile.class))).thenThrow(new IOException("Falha no upload"));

        // Act
        ResponseEntity<UUID> response = barberController.createBarber(createBarberDTO, mockFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(barberService, times(1)).create(createBarberDTO, mockFile);
    }


    @Test
    @DisplayName("Deve realizar login e retornar LoginResponseDTO com status OK 200")
    void login_ShouldReturnLoginResponse_And_StatusOk() {
        // Arrange
        when(barberService.login(any(LoginDTO.class))).thenReturn(loginResponseDTO);

        // Act
        ResponseEntity<LoginResponseDTO> response = barberController.login(loginDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("dummy.token.123", response.getBody().getToken());
        verify(barberService, times(1)).login(loginDTO);
    }

    @Test
    @DisplayName("Deve atualizar barbeiro (logado) e retornar status OK 200")
    void updateBarber_ShouldReturn_StatusOk() {
        // Arrange
        doNothing().when(barberService).update(eq(testEmail), any(BarberDTO.class));

        // Act
        ResponseEntity<Void> response = barberController.updateBarber(mockPrincipal, barberDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(barberService, times(1)).update(testEmail, barberDTO);
    }

    @Test
    @DisplayName("Deve definir horas de trabalho e retornar status NO_CONTENT 204")
    void setBarberWorkHours_ShouldReturn_StatusNoContent() {
        // Arrange
        BarberWorkHoursDTO workHoursDTO = new BarberWorkHoursDTO();
        doNothing().when(barberService).setWorkHours(eq(testEmail), any(BarberWorkHoursDTO.class));

        // Act
        ResponseEntity<Void> response = barberController.setBarberWorkHours(mockPrincipal, workHoursDTO);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(barberService, times(1)).setWorkHours(testEmail, workHoursDTO);
    }

    @Test
    @DisplayName("Deve atribuir atividades ao barbeiro e retornar status NO_CONTENT 204")
    void assignActivitiesToBarber_ShouldReturn_StatusNoContent() {
        // Arrange
        BarberActivityAssignDTO assignDTO = new BarberActivityAssignDTO();
        doNothing().when(barberService).assignActivities(eq(testEmail), any(BarberActivityAssignDTO.class));

        // Act
        ResponseEntity<Void> response = barberController.assignActivitiesToBarber(mockPrincipal, assignDTO);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(barberService, times(1)).assignActivities(testEmail, assignDTO);
    }

    @Test
    @DisplayName("Deve retornar atividades do barbeiro (logado) e status OK 200")
    void getMyActivities_ShouldReturnDtoList_And_StatusOk() {
        // Arrange
        when(barberService.getMyAssignedActivities(testEmail)).thenReturn(List.of(new ActivityDTO()));

        // Act
        ResponseEntity<List<ActivityDTO>> response = barberController.getMyActivities(mockPrincipal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        verify(barberService, times(1)).getMyAssignedActivities(testEmail);
    }

    @Test
    @DisplayName("Deve retornar atividades de um barbeiro por ID e status OK 200")
    void getBarberActivities_ShouldReturnDtoList_And_StatusOk() {
        // Arrange
        when(barberService.listActivitiesByBarber(testUuid)).thenReturn(List.of(new ActivityDTO()));

        // Act
        ResponseEntity<List<ActivityDTO>> response = barberController.getBarberActivities(testUuid);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        verify(barberService, times(1)).listActivitiesByBarber(testUuid);
    }

    @Test
    @DisplayName("Deve retornar histórico de pedidos (Join) do barbeiro e status OK 200")
    void getBarberJoinRequestHistory_ShouldReturnDtoList_And_StatusOk() {
        // Arrange
        when(barberService.getJoinRequestHistory(testEmail)).thenReturn(List.of(new JoinRequestHistoryDTO()));

        // Act
        ResponseEntity<List<JoinRequestHistoryDTO>> response = barberController.getBarberJoinRequestHistory(mockPrincipal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        verify(barberService, times(1)).getJoinRequestHistory(testEmail);
    }

    @Test
    @DisplayName("Deve rejeitar pedido (Join) e retornar status NO_CONTENT 204")
    void rejectJoinRequest_ShouldReturn_StatusNoContent() {
        // Arrange
        Long requestId = 1L;
        doNothing().when(barberService).rejectJoinRequest(testEmail, requestId);

        // Act
        ResponseEntity<Void> response = barberController.rejectJoinRequest(requestId, mockPrincipal);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(barberService, times(1)).rejectJoinRequest(testEmail, requestId);
    }

    @Test
    @DisplayName("Deve deletar barbeiro (logado) e retornar status NO_CONTENT 204")
    void deleteBarber_ShouldReturn_StatusNoContent() {
        // Arrange
        doNothing().when(barberService).delete(testEmail);

        // Act
        ResponseEntity<Void> response = barberController.deleteBarber(mockPrincipal);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(barberService, times(1)).delete(testEmail);
    }

    @Test
    @DisplayName("Deve retornar disponibilidade (slots) do barbeiro e status OK 200")
    void getBarberAvailability_ShouldReturnTimeList_And_StatusOk() {
        // Arrange
        LocalDate date = LocalDate.now();
        int duration = 30;
        when(barberService.getAvailableSlots(testUuid, date, duration)).thenReturn(List.of(LocalTime.of(9, 0)));

        // Act
        ResponseEntity<List<LocalTime>> response = barberController.getBarberAvailability(testUuid, date, duration);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        verify(barberService, times(1)).getAvailableSlots(testUuid, date, duration);
    }

    @Test
    @DisplayName("Deve retornar disponibilidade mensal do barbeiro e status OK 200")
    void getBarberMonthlyAvailability_ShouldReturnDtoList_And_StatusOk() {
        // Arrange
        int year = 2024;
        int month = 10;
        when(barberService.getMonthlyAvailability(testUuid, year, month)).thenReturn(List.of(new DailyAvailabilityDTO(LocalDate.now(), true)));

        // Act
        ResponseEntity<List<DailyAvailabilityDTO>> response = barberController.getBarberMonthlyAvailability(testUuid, year, month);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        verify(barberService, times(1)).getMonthlyAvailability(testUuid, year, month);
    }

    @Test
    @DisplayName("Deve fazer upload da foto de perfil e retornar URL com status OK 200")
    void uploadBarberPhoto_ShouldReturnUrl_And_StatusOk() throws IOException {
        // Arrange
        String imageUrl = "http://imagem.url/foto.jpg";
        when(barberService.updateBarberProfilePhoto(testEmail, mockFile)).thenReturn(imageUrl);

        // Act
        ResponseEntity<String> response = barberController.uploadBarberPhoto(mockPrincipal, mockFile);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(imageUrl, response.getBody());
        verify(barberService, times(1)).updateBarberProfilePhoto(testEmail, mockFile);
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR 500 ao falhar no upload da foto de perfil")
    void uploadBarberPhoto_WhenUploadFails_ShouldReturnStatus500() throws IOException {
        // Arrange
        when(barberService.updateBarberProfilePhoto(testEmail, mockFile)).thenThrow(new IOException("Falha no upload"));

        // Act
        ResponseEntity<String> response = barberController.uploadBarberPhoto(mockPrincipal, mockFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Falha no upload"));
        verify(barberService, times(1)).updateBarberProfilePhoto(testEmail, mockFile);
    }
}
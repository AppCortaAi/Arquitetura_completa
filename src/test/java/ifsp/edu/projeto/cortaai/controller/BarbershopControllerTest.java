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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para BarbershopController")
class BarbershopControllerTest {

    @Mock
    private BarberService barberService; // A única dependência, mocada

    @InjectMocks
    private BarbershopController barbershopController; // Classe sob teste

    @Mock
    private Principal mockPrincipal; // Mock para usuário logado

    private final UUID testShopId = UUID.randomUUID();
    private final UUID testBarberId = UUID.randomUUID();
    private final UUID testActivityId = UUID.randomUUID();
    private final String testEmail = "dono@barbearia.com";
    private final String imageUrl = "http://imagem.url/foto.jpg";

    private BarbershopDTO barbershopDTO;
    private ActivityDTO activityDTO;
    private BarberDTO barberDTO;
    private CreateBarbershopDTO createBarbershopDTO;
    private CreateActivityDTO createActivityDTO;
    private UpdateActivityDTO updateActivityDTO;
    private UpdateBarbershopDTO updateBarbershopDTO;
    private CloseBarbershopRequestDTO closeBarbershopRequestDTO;
    private BarberJoinRequestDTO barberJoinRequestDTO;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // Configura mocks de DTOs
        barbershopDTO = new BarbershopDTO();
        barbershopDTO.setId(testShopId);

        activityDTO = new ActivityDTO();
        activityDTO.setId(testActivityId);

        barberDTO = new BarberDTO();
        barberDTO.setId(testBarberId);

        createBarbershopDTO = new CreateBarbershopDTO();
        createActivityDTO = new CreateActivityDTO();
        updateActivityDTO = new UpdateActivityDTO();
        updateBarbershopDTO = new UpdateBarbershopDTO();
        closeBarbershopRequestDTO = new CloseBarbershopRequestDTO();

        barberJoinRequestDTO = new BarberJoinRequestDTO();
        barberJoinRequestDTO.setCnpj("12345678901234");

        mockFile = new MockMultipartFile(
                "file",
                "imagem.png",
                "image/png",
                "conteudo-da-imagem".getBytes()
        );

        // Configuração padrão do Principal
        lenient().when(mockPrincipal.getName()).thenReturn(testEmail);
    }

    // --- Testes de Endpoints Públicos ---

    @Test
    @DisplayName("Deve listar todas as barbearias e retornar status OK 200")
    void listAllBarbershops_ShouldReturnList_And_StatusOk() {
        // Arrange
        when(barberService.listBarbershops()).thenReturn(List.of(barbershopDTO));

        // Act
        ResponseEntity<List<BarbershopDTO>> response = barbershopController.listAllBarbershops();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        verify(barberService, times(1)).listBarbershops();
    }

    @Test
    @DisplayName("Deve listar serviços da barbearia e retornar status OK 200")
    void listServicesForBarbershop_ShouldReturnList_And_StatusOk() {
        // Arrange
        when(barberService.listActivities(testShopId)).thenReturn(List.of(activityDTO));

        // Act
        ResponseEntity<List<ActivityDTO>> response = barbershopController.listServicesForBarbershop(testShopId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        verify(barberService, times(1)).listActivities(testShopId);
    }

    @Test
    @DisplayName("Deve listar barbeiros da barbearia e retornar status OK 200")
    void listBarbersForBarbershop_ShouldReturnList_And_StatusOk() {
        // Arrange
        when(barberService.listBarbersByBarbershop(testShopId)).thenReturn(List.of(barberDTO));

        // Act
        ResponseEntity<List<BarberDTO>> response = barbershopController.listBarbersForBarbershop(testShopId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        verify(barberService, times(1)).listBarbersByBarbershop(testShopId);
    }

    // --- Testes de Fluxo 1: Gestão do Dono (Owner) ---

    @Test
    @DisplayName("Deve registrar barbearia e retornar DTO com status CREATED 201")
    void createBarbershop_ShouldReturnDto_And_StatusCreated() throws IOException {
        // Arrange
        when(barberService.createBarbershop(testEmail, createBarbershopDTO, mockFile)).thenReturn(barbershopDTO);

        // Act
        ResponseEntity<BarbershopDTO> response = barbershopController.createBarbershop(mockPrincipal, createBarbershopDTO, mockFile);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(barbershopDTO.getId(), response.getBody().getId());
        verify(barberService, times(1)).createBarbershop(testEmail, createBarbershopDTO, mockFile);
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR 500 se o registro da barbearia falhar (IOException)")
    void createBarbershop_WhenUploadFails_ShouldReturnStatus500() throws IOException {
        // Arrange
        when(barberService.createBarbershop(testEmail, createBarbershopDTO, mockFile)).thenThrow(new IOException("Falha no upload"));

        // Act
        ResponseEntity<BarbershopDTO> response = barbershopController.createBarbershop(mockPrincipal, createBarbershopDTO, mockFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Deve criar atividade e retornar DTO com status CREATED 201")
    void createActivities_ShouldReturnDto_And_StatusCreated() {
        // Arrange
        when(barberService.createActivities(testEmail, createActivityDTO)).thenReturn(activityDTO);

        // Act
        ResponseEntity<ActivityDTO> response = barbershopController.createActivities(mockPrincipal, createActivityDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(activityDTO.getId(), response.getBody().getId());
        verify(barberService, times(1)).createActivities(testEmail, createActivityDTO);
    }

    @Test
    @DisplayName("Deve atualizar atividade e retornar DTO com status OK 200")
    void updateActivity_ShouldReturnDto_And_StatusOk() {
        // Arrange
        when(barberService.updateActivity(testEmail, testActivityId, updateActivityDTO)).thenReturn(activityDTO);

        // Act
        ResponseEntity<ActivityDTO> response = barbershopController.updateActivity(mockPrincipal, testActivityId, updateActivityDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(activityDTO.getId(), response.getBody().getId());
        verify(barberService, times(1)).updateActivity(testEmail, testActivityId, updateActivityDTO);
    }

    @Test
    @DisplayName("Deve deletar atividade e retornar status NO_CONTENT 204")
    void deleteActivity_ShouldReturn_StatusNoContent() {
        // Arrange
        doNothing().when(barberService).deleteActivity(testEmail, testActivityId);

        // Act
        ResponseEntity<Void> response = barbershopController.deleteActivity(mockPrincipal, testActivityId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(barberService, times(1)).deleteActivity(testEmail, testActivityId);
    }

    @Test
    @DisplayName("Deve atualizar barbearia e retornar DTO com status OK 200")
    void updateBarbershop_ShouldReturnDto_And_StatusOk() {
        // Arrange
        when(barberService.updateBarbershop(testEmail, updateBarbershopDTO)).thenReturn(barbershopDTO);

        // Act
        ResponseEntity<BarbershopDTO> response = barbershopController.updateBarbershop(mockPrincipal, updateBarbershopDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(barberService, times(1)).updateBarbershop(testEmail, updateBarbershopDTO);
    }

    @Test
    @DisplayName("Deve remover barbeiro e retornar status NO_CONTENT 204")
    void removeBarber_ShouldReturn_StatusNoContent() {
        // Arrange
        doNothing().when(barberService).removeBarber(testEmail, testBarberId);

        // Act
        ResponseEntity<Void> response = barbershopController.removeBarber(mockPrincipal, testBarberId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(barberService, times(1)).removeBarber(testEmail, testBarberId);
    }

    @Test
    @DisplayName("Deve fechar barbearia e retornar status NO_CONTENT 204")
    void closeBarbershop_ShouldReturn_StatusNoContent() {
        // Arrange
        doNothing().when(barberService).closeBarbershop(testEmail, closeBarbershopRequestDTO);

        // Act
        ResponseEntity<Void> response = barbershopController.closeBarbershop(mockPrincipal, closeBarbershopRequestDTO);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(barberService, times(1)).closeBarbershop(testEmail, closeBarbershopRequestDTO);
    }

    @Test
    @DisplayName("Deve listar pedidos pendentes e retornar status OK 200")
    void getPendingRequests_ShouldReturnList_And_StatusOk() {
        // Arrange
        when(barberService.getPendingJoinRequests(testEmail)).thenReturn(List.of(new JoinRequestDTO()));

        // Act
        ResponseEntity<List<JoinRequestDTO>> response = barbershopController.getPendingRequests(mockPrincipal);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        verify(barberService, times(1)).getPendingJoinRequests(testEmail);
    }

    @Test
    @DisplayName("Deve aprovar pedido e retornar status NO_CONTENT 204")
    void approveJoinRequest_ShouldReturn_StatusNoContent() {
        // Arrange
        Long requestId = 1L;
        doNothing().when(barberService).approveJoinRequest(testEmail, requestId);

        // Act
        ResponseEntity<Void> response = barbershopController.approveJoinRequest(mockPrincipal, requestId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(barberService, times(1)).approveJoinRequest(testEmail, requestId);
    }

    // --- Testes de Fluxo 2: Gestão do Staff (Barbeiro) ---

    @Test
    @DisplayName("Deve solicitar entrada (join) e retornar status ACCEPTED 202")
    void requestToJoinBarbershop_ShouldReturn_StatusAccepted() {
        // Arrange
        doNothing().when(barberService).requestToJoinBarbershop(testEmail, barberJoinRequestDTO.getCnpj());

        // Act
        ResponseEntity<Void> response = barbershopController.requestToJoinBarbershop(mockPrincipal, barberJoinRequestDTO);

        // Assert
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(barberService, times(1)).requestToJoinBarbershop(testEmail, barberJoinRequestDTO.getCnpj());
    }

    // --- Testes de Fluxo 3: Sair da Loja ---

    @Test
    @DisplayName("Deve sair da loja (free) e retornar status NO_CONTENT 204")
    void freeBarber_ShouldReturn_StatusNoContent() {
        // Arrange
        doNothing().when(barberService).freeBarber(testEmail);

        // Act
        ResponseEntity<Void> response = barbershopController.freeBarber(mockPrincipal);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(barberService, times(1)).freeBarber(testEmail);
    }

    // --- Testes de Fluxo 4: Gestão de Imagens (Uploads) ---

    @Test
    @DisplayName("Deve fazer upload do logo e retornar URL com status OK 200")
    void uploadBarbershopLogo_ShouldReturnUrl_And_StatusOk() throws IOException {
        // Arrange
        when(barberService.updateBarbershopLogo(testEmail, mockFile)).thenReturn(imageUrl);

        // Act
        ResponseEntity<String> response = barbershopController.uploadBarbershopLogo(mockPrincipal, mockFile);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(imageUrl, response.getBody());
        verify(barberService, times(1)).updateBarbershopLogo(testEmail, mockFile);
    }

    @Test
    @DisplayName("Deve falhar no upload do logo e retornar status 500")
    void uploadBarbershopLogo_WhenFails_ShouldReturnStatus500() throws IOException {
        // Arrange
        when(barberService.updateBarbershopLogo(testEmail, mockFile)).thenThrow(new IOException("Erro"));

        // Act
        ResponseEntity<String> response = barbershopController.uploadBarbershopLogo(mockPrincipal, mockFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve fazer upload do banner e retornar URL com status OK 200")
    void uploadBarbershopBanner_ShouldReturnUrl_And_StatusOk() throws IOException {
        // Arrange
        when(barberService.updateBarbershopBanner(testEmail, mockFile)).thenReturn(imageUrl);

        // Act
        ResponseEntity<String> response = barbershopController.uploadBarbershopBanner(mockPrincipal, mockFile);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(imageUrl, response.getBody());
    }

    @Test
    @DisplayName("Deve falhar no upload do banner e retornar status 500")
    void uploadBarbershopBanner_WhenFails_ShouldReturnStatus500() throws IOException {
        // Arrange
        when(barberService.updateBarbershopBanner(testEmail, mockFile)).thenThrow(new IOException("Erro"));

        // Act
        ResponseEntity<String> response = barbershopController.uploadBarbershopBanner(mockPrincipal, mockFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve fazer upload da foto da atividade e retornar URL com status OK 200")
    void uploadActivityPhoto_ShouldReturnUrl_And_StatusOk() throws IOException {
        // Arrange
        when(barberService.updateActivityPhoto(testEmail, testActivityId, mockFile)).thenReturn(imageUrl);

        // Act
        ResponseEntity<String> response = barbershopController.uploadActivityPhoto(mockPrincipal, testActivityId, mockFile);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(imageUrl, response.getBody());
    }

    @Test
    @DisplayName("Deve falhar no upload da foto da atividade e retornar status 500")
    void uploadActivityPhoto_WhenFails_ShouldReturnStatus500() throws IOException {
        // Arrange
        when(barberService.updateActivityPhoto(testEmail, testActivityId, mockFile)).thenThrow(new IOException("Erro"));

        // Act
        ResponseEntity<String> response = barbershopController.uploadActivityPhoto(mockPrincipal, testActivityId, mockFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve adicionar destaque e retornar URL com status CREATED 201")
    void addBarbershopHighlight_ShouldReturnUrl_And_StatusCreated() throws IOException {
        // Arrange
        when(barberService.addBarbershopHighlight(testEmail, mockFile)).thenReturn(imageUrl);

        // Act
        ResponseEntity<String> response = barbershopController.addBarbershopHighlight(mockPrincipal, mockFile);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(imageUrl, response.getBody());
    }

    @Test
    @DisplayName("Deve falhar ao adicionar destaque e retornar status 500")
    void addBarbershopHighlight_WhenFails_ShouldReturnStatus500() throws IOException {
        // Arrange
        when(barberService.addBarbershopHighlight(testEmail, mockFile)).thenThrow(new IOException("Erro"));

        // Act
        ResponseEntity<String> response = barbershopController.addBarbershopHighlight(mockPrincipal, mockFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve deletar destaque e retornar status NO_CONTENT 204")
    void deleteBarbershopHighlight_ShouldReturn_StatusNoContent() {
        // Arrange
        doNothing().when(barberService).deleteBarbershopHighlight(testEmail, testShopId);

        // Act
        ResponseEntity<Void> response = barbershopController.deleteBarbershopHighlight(mockPrincipal, testShopId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(barberService, times(1)).deleteBarbershopHighlight(testEmail, testShopId);
    }
}
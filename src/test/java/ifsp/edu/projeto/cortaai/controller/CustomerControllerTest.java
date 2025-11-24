package ifsp.edu.projeto.cortaai.controller;

import ifsp.edu.projeto.cortaai.dto.CustomerCreateDTO;
import ifsp.edu.projeto.cortaai.dto.CustomerDTO;
import ifsp.edu.projeto.cortaai.dto.LoginDTO;
import ifsp.edu.projeto.cortaai.dto.LoginResponseDTO;
import ifsp.edu.projeto.cortaai.service.CustomerService;
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
@DisplayName("Testes para CustomerController")
class CustomerControllerTest {

    @Mock
    private CustomerService customerService; // Dependência mocada

    @InjectMocks
    private CustomerController customerController; // Classe sob teste

    @Mock
    private Principal mockPrincipal; // Mock para usuário logado (Cliente)

    private final UUID testUuid = UUID.randomUUID();
    private final String testEmail = "cliente@teste.com";
    private final String imageUrl = "http://imagem.url/foto.jpg";

    private CustomerDTO customerDTO;
    private CustomerCreateDTO customerCreateDTO;
    private LoginDTO loginDTO;
    private LoginResponseDTO loginResponseDTO;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        customerDTO = new CustomerDTO();
        customerDTO.setId(testUuid);
        customerDTO.setEmail(testEmail);
        customerDTO.setName("Cliente Teste");

        customerCreateDTO = new CustomerCreateDTO();
        customerCreateDTO.setEmail(testEmail);
        customerCreateDTO.setPassword("123456");

        loginDTO = new LoginDTO();
        loginDTO.setEmail(testEmail);
        loginDTO.setPassword("123456");

        loginResponseDTO = LoginResponseDTO.builder()
                .token("dummy.token.456")
                .userData(customerDTO)
                .build();

        mockFile = new MockMultipartFile(
                "file",
                "foto_cliente.jpg",
                "image/jpeg",
                "some-image-bytes".getBytes()
        );

        // Configuração padrão para o mock do Principal
        lenient().when(mockPrincipal.getName()).thenReturn(testEmail);
    }

    @Test
    @DisplayName("Deve retornar lista de DTOs de Clientes e status OK 200")
    void getAllCustomers_ShouldReturnDtoList_And_StatusOk() {
        // Arrange
        when(customerService.findAll()).thenReturn(List.of(customerDTO));

        // Act
        ResponseEntity<List<CustomerDTO>> response = customerController.getAllCustomers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(customerService, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar um DTO de Cliente quando ID existe e status OK 200")
    void getCustomer_ShouldReturnDto_And_StatusOk() {
        // Arrange
        when(customerService.get(testUuid)).thenReturn(customerDTO);

        // Act
        ResponseEntity<CustomerDTO> response = customerController.getCustomer(testUuid);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customerDTO.getId(), response.getBody().getId());
        verify(customerService, times(1)).get(testUuid);
    }

    @Test
    @DisplayName("Deve criar cliente e retornar UUID com status CREATED 201 (com arquivo)")
    void createCustomer_WithFile_ShouldReturnCreatedId_And_StatusCreated() throws IOException {
        // Arrange
        when(customerService.create(any(CustomerCreateDTO.class), any(MultipartFile.class))).thenReturn(testUuid);

        // Act
        ResponseEntity<UUID> response = customerController.createCustomer(customerCreateDTO, mockFile);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testUuid, response.getBody());
        verify(customerService, times(1)).create(customerCreateDTO, mockFile);
    }

    @Test
    @DisplayName("Deve criar cliente e retornar UUID com status CREATED 201 (sem arquivo)")
    void createCustomer_WithoutFile_ShouldReturnCreatedId_And_StatusCreated() throws IOException {
        // Arrange
        // O controller envia 'null' para o service se 'required=false' e o arquivo não vier
        when(customerService.create(any(CustomerCreateDTO.class), isNull())).thenReturn(testUuid);

        // Act
        ResponseEntity<UUID> response = customerController.createCustomer(customerCreateDTO, null);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testUuid, response.getBody());
        verify(customerService, times(1)).create(customerCreateDTO, null);
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR 500 ao falhar no upload do registro")
    void createCustomer_WhenUploadFails_ShouldReturnStatus500() throws IOException {
        // Arrange
        when(customerService.create(any(CustomerCreateDTO.class), any(MultipartFile.class))).thenThrow(new IOException("Falha no upload"));

        // Act
        ResponseEntity<UUID> response = customerController.createCustomer(customerCreateDTO, mockFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Deve realizar login e retornar LoginResponseDTO com status OK 200")
    void login_ShouldReturnLoginResponse_And_StatusOk() {
        // Arrange
        when(customerService.login(any(LoginDTO.class))).thenReturn(loginResponseDTO);

        // Act
        ResponseEntity<LoginResponseDTO> response = customerController.login(loginDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponseDTO.getToken(), response.getBody().getToken());
        verify(customerService, times(1)).login(loginDTO);
    }

    @Test
    @DisplayName("Deve atualizar cliente (logado) e retornar status OK 200")
    void updateCustomer_ShouldReturn_StatusOk() {
        // Arrange
        doNothing().when(customerService).update(eq(testEmail), any(CustomerDTO.class));

        // Act
        ResponseEntity<Void> response = customerController.updateCustomer(mockPrincipal, customerDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(customerService, times(1)).update(testEmail, customerDTO);
    }

    @Test
    @DisplayName("Deve deletar cliente (logado) e retornar status NO_CONTENT 204")
    void deleteCustomer_ShouldReturn_StatusNoContent() {
        // Arrange
        doNothing().when(customerService).delete(testEmail);

        // Act
        ResponseEntity<Void> response = customerController.deleteCustomer(mockPrincipal);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerService, times(1)).delete(testEmail);
    }

    @Test
    @DisplayName("Deve fazer upload da foto de perfil e retornar URL com status OK 200")
    void uploadCustomerPhoto_ShouldReturnUrl_And_StatusOk() throws IOException {
        // Arrange
        when(customerService.updateProfilePhoto(testEmail, mockFile)).thenReturn(imageUrl);

        // Act
        ResponseEntity<String> response = customerController.uploadCustomerPhoto(mockPrincipal, mockFile);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(imageUrl, response.getBody());
        verify(customerService, times(1)).updateProfilePhoto(testEmail, mockFile);
    }

    @Test
    @DisplayName("Deve retornar INTERNAL_SERVER_ERROR 500 ao falhar no upload da foto de perfil")
    void uploadCustomerPhoto_WhenUploadFails_ShouldReturnStatus500() throws IOException {
        // Arrange
        when(customerService.updateProfilePhoto(testEmail, mockFile)).thenThrow(new IOException("Falha no upload"));

        // Act
        ResponseEntity<String> response = customerController.uploadCustomerPhoto(mockPrincipal, mockFile);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Falha no upload"));
    }
}
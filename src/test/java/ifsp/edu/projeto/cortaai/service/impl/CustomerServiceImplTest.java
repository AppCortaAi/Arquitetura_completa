package ifsp.edu.projeto.cortaai.service.impl;

import ifsp.edu.projeto.cortaai.dto.CustomerCreateDTO;
import ifsp.edu.projeto.cortaai.dto.CustomerDTO;
import ifsp.edu.projeto.cortaai.dto.LoginDTO;
import ifsp.edu.projeto.cortaai.dto.LoginResponseDTO;
import ifsp.edu.projeto.cortaai.dto.UploadResultDTO;
import ifsp.edu.projeto.cortaai.events.BeforeDeleteCustomer;
import ifsp.edu.projeto.cortaai.exception.NotFoundException;
import ifsp.edu.projeto.cortaai.mapper.CustomerMapper;
import ifsp.edu.projeto.cortaai.model.Customer;
import ifsp.edu.projeto.cortaai.repository.CustomerRepository;
import ifsp.edu.projeto.cortaai.service.JwtTokenService;
import ifsp.edu.projeto.cortaai.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para CustomerServiceImpl")
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private CustomerMapper customerMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private StorageService storageService;
    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;
    @Captor
    private ArgumentCaptor<BeforeDeleteCustomer> eventCaptor;

    // --- Mocks de Entidades e DTOs ---
    private Customer mockCustomer;
    private CustomerDTO customerDTO;
    private CustomerCreateDTO customerCreateDTO;
    private LoginDTO loginDTO;
    private UploadResultDTO mockUploadResult;

    // --- Dados Comuns ---
    private final UUID customerId = UUID.randomUUID();
    private final String customerEmail = "cliente@teste.com";
    private final String rawPassword = "password123";
    private final String hashedPassword = "hashedPassword123";
    private final String imageUrl = "http://imagem.url/foto.jpg";
    private final String imagePublicId = "folder/public_id";

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setEmail(customerEmail);
        mockCustomer.setPassword(hashedPassword);
        mockCustomer.setImageUrlPublicId(imagePublicId); // Cliente já tem uma foto

        customerDTO = new CustomerDTO();
        customerDTO.setEmail(customerEmail);
        customerDTO.setName("Cliente Teste");

        customerCreateDTO = new CustomerCreateDTO();
        customerCreateDTO.setEmail("novo@cliente.com");
        customerCreateDTO.setPassword(rawPassword);

        loginDTO = new LoginDTO();
        loginDTO.setEmail(customerEmail);
        loginDTO.setPassword(rawPassword);

        mockUploadResult = new UploadResultDTO(imagePublicId, imageUrl);

        // Mocks Padrão
        lenient().when(customerMapper.toDTO(any(Customer.class))).thenReturn(customerDTO);
    }

    // --- Testes de login() ---

    @Test
    @DisplayName("login: Deve retornar LoginResponseDTO se a senha bater")
    void login_ShouldReturnLoginResponse_WhenPasswordMatches() {
        // Arrange
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtTokenService.generateToken(mockCustomer)).thenReturn("token123");
        // customerMapper.toDTO já mocado no setUp

        // Act
        LoginResponseDTO response = customerService.login(loginDTO);

        // Assert
        assertNotNull(response);
        assertEquals("token123", response.getToken());
        assertEquals(customerDTO, response.getUserData());
        verify(customerRepository, times(1)).findByEmail(customerEmail);
        verify(passwordEncoder, times(1)).matches(rawPassword, hashedPassword);
    }

    @Test
    @DisplayName("login: Deve lançar NotFoundException se o cliente não for encontrado")
    void login_ShouldThrowNotFound_WhenCustomerNotFound() {
        // Arrange
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            customerService.login(loginDTO);
        }, "Usuário ou senha inválidos");
    }

    @Test
    @DisplayName("login: Deve lançar NotFoundException se a senha não bater")
    void login_ShouldThrowNotFound_WhenPasswordDoesNotMatch() {
        // Arrange
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            customerService.login(loginDTO);
        }, "Usuário ou senha inválidos");
    }

    // --- Testes de create() ---

    @Test
    @DisplayName("create: Deve criar cliente e encodar senha (sem arquivo)")
    void create_ShouldCreateCustomer_AndEncodePassword_WithoutFile() throws IOException {
        // Arrange
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);

        // Act
        UUID newId = customerService.create(customerCreateDTO, null); // Sem arquivo

        // Assert
        assertEquals(customerId, newId);
        // Verifica se o save foi chamado apenas UMA vez
        verify(customerRepository, times(1)).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertEquals(hashedPassword, savedCustomer.getPassword());
        assertEquals(customerCreateDTO.getEmail(), savedCustomer.getEmail());

        // Garante que o storage não foi chamado
        verify(storageService, never()).uploadFile(any(), any());
    }

    @Test
    @DisplayName("create: Deve criar cliente, encodar senha e fazer upload (com arquivo)")
    void create_ShouldCreateCustomer_EncodePassword_AndUploadFile() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        Customer customerSemImagem = new Customer(); // Primeiro save
        customerSemImagem.setId(customerId);

        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        // Primeiro save (sem imagem)
        when(customerRepository.save(any(Customer.class))).thenReturn(customerSemImagem);
        // Mock do upload
        when(storageService.uploadFile(mockFile, "customer-profiles")).thenReturn(mockUploadResult);

        // Act
        UUID newId = customerService.create(customerCreateDTO, mockFile);

        // Assert
        assertEquals(customerId, newId);
        // Verifica se o storage foi chamado
        verify(storageService, times(1)).uploadFile(mockFile, "customer-profiles");

        // Verifica se o save foi chamado DUAS vezes
        verify(customerRepository, times(2)).save(customerCaptor.capture());

        // Verifica o segundo save (com a imagem)
        Customer finalCustomer = customerCaptor.getValue();
        assertEquals(imageUrl, finalCustomer.getImageUrl());
        assertEquals(imagePublicId, finalCustomer.getImageUrlPublicId());
    }

    // --- Testes de update() ---

    @Test
    @DisplayName("update: Deve encontrar por e-mail e salvar atualizações")
    void update_ShouldFindByEmail_AndSaveUpdates() {
        // Arrange
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));

        CustomerDTO updates = new CustomerDTO();
        updates.setName("Nome Atualizado");
        updates.setTell("123456");
        updates.setEmail("novo@email.com"); // Email também atualiza
        updates.setDocumentCPF("111222333");
        updates.setImageUrl("nova.url"); // Imagem deve ser atualizada

        // Act
        customerService.update(customerEmail, updates);

        // Assert
        verify(customerRepository, times(1)).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        assertEquals("Nome Atualizado", savedCustomer.getName());
        assertEquals("123456", savedCustomer.getTell());
        assertEquals("novo@email.com", savedCustomer.getEmail());
        assertEquals("111222333", savedCustomer.getDocumentCPF());
        assertEquals("nova.url", savedCustomer.getImageUrl());
    }

    @Test
    @DisplayName("update: Não deve atualizar a imagem se a URL no DTO for nula")
    void update_ShouldNotUpdateImage_WhenDtoUrlIsNull() {
        // Arrange
        mockCustomer.setImageUrl("url.antiga");
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));

        CustomerDTO updates = new CustomerDTO();
        updates.setName("Nome Atualizado");
        updates.setImageUrl(null); // URL nula

        // Act
        customerService.update(customerEmail, updates);

        // Assert
        verify(customerRepository, times(1)).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        assertEquals("Nome Atualizado", savedCustomer.getName());
        assertEquals("url.antiga", savedCustomer.getImageUrl()); // Deve manter a antiga
    }

    // --- Testes de delete() ---

    @Test
    @DisplayName("delete: Deve publicar evento BeforeDeleteCustomer e chamar delete")
    void delete_ShouldPublishEvent_AndCallDelete() {
        // Arrange
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        doNothing().when(publisher).publishEvent(any(BeforeDeleteCustomer.class));
        doNothing().when(customerRepository).delete(mockCustomer);

        // Act
        customerService.delete(customerEmail);

        // Assert
        verify(publisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals(customerId, eventCaptor.getValue().getId()); // Verifica se o ID no evento está correto

        verify(customerRepository, times(1)).delete(mockCustomer);
    }

    // --- Testes de updateProfilePhoto() ---

    @Test
    @DisplayName("updateProfilePhoto: Deve deletar foto antiga e fazer upload da nova")
    void updateProfilePhoto_ShouldDeleteOldPhoto_AndUploadNew() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        UploadResultDTO newUpload = new UploadResultDTO("new_public_id", "new_image_url");

        // mockCustomer (do setUp) tem um publicId antigo
        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        doNothing().when(storageService).deleteFile(imagePublicId); // Deleta o antigo
        when(storageService.uploadFile(mockFile, "customer-profiles")).thenReturn(newUpload); // Upload do novo

        // Act
        String resultUrl = customerService.updateProfilePhoto(customerEmail, mockFile);

        // Assert
        assertEquals("new_image_url", resultUrl);
        // Verifica a ordem das interações com o storage
        verify(storageService, times(1)).deleteFile(imagePublicId);
        verify(storageService, times(1)).uploadFile(mockFile, "customer-profiles");

        // Verifica se o cliente foi salvo com os novos dados
        verify(customerRepository, times(1)).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertEquals("new_image_url", savedCustomer.getImageUrl());
        assertEquals("new_public_id", savedCustomer.getImageUrlPublicId());
    }

    @Test
    @DisplayName("updateProfilePhoto: Deve apenas fazer upload se não houver foto antiga")
    void updateProfilePhoto_ShouldOnlyUpload_WhenNoOldPhoto() throws IOException {
        // Arrange
        mockCustomer.setImageUrlPublicId(null); // Cliente sem foto antiga
        MultipartFile mockFile = mock(MultipartFile.class);
        UploadResultDTO newUpload = new UploadResultDTO("new_public_id", "new_image_url");

        when(customerRepository.findByEmail(customerEmail)).thenReturn(Optional.of(mockCustomer));
        when(storageService.uploadFile(mockFile, "customer-profiles")).thenReturn(newUpload);

        // Act
        String resultUrl = customerService.updateProfilePhoto(customerEmail, mockFile);

        // Assert
        assertEquals("new_image_url", resultUrl);
        // Garante que delete NUNCA foi chamado
        verify(storageService, never()).deleteFile(anyString());
        verify(storageService, times(1)).uploadFile(mockFile, "customer-profiles");

        verify(customerRepository, times(1)).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertEquals("new_image_url", savedCustomer.getImageUrl());
        assertEquals("new_public_id", savedCustomer.getImageUrlPublicId());
    }
}
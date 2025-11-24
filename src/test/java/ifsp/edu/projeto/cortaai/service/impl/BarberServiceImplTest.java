package ifsp.edu.projeto.cortaai.service.impl;

import ifsp.edu.projeto.cortaai.dto.*;
import ifsp.edu.projeto.cortaai.events.BeforeDeleteBarber;
import ifsp.edu.projeto.cortaai.exception.NotFoundException;
import ifsp.edu.projeto.cortaai.exception.ReferenceException;
import ifsp.edu.projeto.cortaai.mapper.ActivityMapper;
import ifsp.edu.projeto.cortaai.mapper.BarberMapper;
import ifsp.edu.projeto.cortaai.mapper.BarbershopMapper;
import ifsp.edu.projeto.cortaai.model.*;
import ifsp.edu.projeto.cortaai.model.enums.AppointmentStatus;
import ifsp.edu.projeto.cortaai.model.enums.JoinRequestStatus;
import ifsp.edu.projeto.cortaai.repository.*;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para BarberServiceImpl (Facade)")
class BarberServiceImplTest {

    // 13 Dependências Mocadas
    @Mock
    private BarberRepository barberRepository;
    @Mock
    private BarbershopRepository barbershopRepository;
    @Mock
    private BarbershopJoinRequestRepository joinRequestRepository;
    @Mock
    private ActivityRepository activityRepository;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private BarberMapper barberMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private BarbershopMapper barbershopMapper;
    @Mock
    private ActivityMapper activityMapper;
    @Mock
    private StorageService storageService;
    @Mock
    private BarbershopHighlightRepository barbershopHighlightRepository;
    @Mock
    private AppointmentsRepository appointmentsRepository;
    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private BarberServiceImpl barberService;

    @Captor
    private ArgumentCaptor<Barber> barberCaptor;
    @Captor
    private ArgumentCaptor<Barbershop> barbershopCaptor;

    // --- Mocks de Entidades ---
    private Barber mockBarber; // Barbeiro staff
    private Barber mockOwner;  // Barbeiro dono
    private Barbershop mockBarbershop;
    private Activity mockActivity;
    private UploadResultDTO mockUploadResult;
    private LoginResponseDTO mockLoginResponse;

    // --- Mocks de DTOs ---
    private CreateBarberDTO createBarberDTO;
    private CreateBarbershopDTO createBarbershopDTO;
    private LoginDTO loginDTO;

    // --- Dados Comuns ---
    private final String barberEmail = "barbeiro@teste.com";
    private final String ownerEmail = "dono@teste.com";
    private final String rawPassword = "password123";
    private final String hashedPassword = "hashedPassword123";
    private final String cnpj = "12345678901234";
    private final UUID barberId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();
    private final UUID barbershopId = UUID.randomUUID();
    private final UUID activityId = UUID.randomUUID();
    private final String imageUrl = "http://imagem.url/foto.jpg";
    private final String imagePublicId = "folder/public_id";

    @BeforeEach
    void setUp() {
        // Injeta o valor do 'slotIntervalMinutes'
        ReflectionTestUtils.setField(barberService, "slotIntervalMinutes", 15);

        // --- Configuração das Entidades ---
        mockBarbershop = new Barbershop();
        mockBarbershop.setId(barbershopId);
        mockBarbershop.setName("Barbearia Teste");
        mockBarbershop.setCnpj(cnpj);

        mockBarber = new Barber();
        mockBarber.setId(barberId);
        mockBarber.setEmail(barberEmail);
        mockBarber.setPassword(hashedPassword);
        mockBarber.setOwner(false);
        mockBarber.setBarbershop(mockBarbershop); // Staff pertence à loja
        mockBarber.setActivities(new HashSet<>()); // Garante que a coleção não é nula

        mockOwner = new Barber();
        mockOwner.setId(ownerId);
        mockOwner.setEmail(ownerEmail);
        mockOwner.setPassword(hashedPassword);
        mockOwner.setOwner(true);
        mockOwner.setBarbershop(mockBarbershop); // Dono pertence à loja
        mockOwner.setActivities(new HashSet<>()); // Garante que a coleção não é nula

        mockActivity = new Activity();
        mockActivity.setId(activityId);
        mockActivity.setBarbershop(mockBarbershop);

        // --- Configuração dos DTOs ---
        loginDTO = new LoginDTO();
        loginDTO.setEmail(barberEmail);
        loginDTO.setPassword(rawPassword);

        createBarberDTO = new CreateBarberDTO();
        createBarberDTO.setName("Novo Barbeiro");
        createBarberDTO.setEmail("novo@email.com");
        createBarberDTO.setPassword(rawPassword);

        createBarbershopDTO = new CreateBarbershopDTO();
        createBarbershopDTO.setName("Nova Barbearia");
        createBarbershopDTO.setCnpj(cnpj);

        // --- Mocks de Serviços/Utils ---
        mockUploadResult = new UploadResultDTO(imagePublicId, imageUrl);

        mockLoginResponse = LoginResponseDTO.builder().token("token").build();

        // Lenient stubs para DTOs (podem não ser usados em todos os testes)
        lenient().when(barberMapper.toDTO(any(Barber.class))).thenReturn(new BarberDTO());
        lenient().when(barbershopMapper.toDTO(any(Barbershop.class))).thenReturn(new BarbershopDTO());
        lenient().when(barbershopMapper.toEntity(any(CreateBarbershopDTO.class))).thenReturn(new Barbershop());
        lenient().when(activityMapper.toEntity(any(CreateActivityDTO.class))).thenReturn(new Activity());
    }

    // --- Fluxo de Gestão de Barbeiro/Autenticação ---

    @Test
    @DisplayName("login: Deve logar com sucesso se a senha bater")
    void login_ShouldReturnResponse_WhenPasswordMatches() {
        // Arrange
        when(barberRepository.findByEmail(barberEmail)).thenReturn(Optional.of(mockBarber));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtTokenService.generateToken(eq(mockBarber))).thenReturn("token");
        when(barberMapper.toDTO(mockBarber)).thenReturn(new BarberDTO());

        // Act
        LoginResponseDTO response = barberService.login(loginDTO);

        // Assert
        assertNotNull(response);
        assertEquals("token", response.getToken());
        verify(barberRepository, times(1)).findByEmail(barberEmail);
        verify(passwordEncoder, times(1)).matches(rawPassword, hashedPassword);
    }

    @Test
    @DisplayName("login: Deve lançar NotFoundException se a senha não bater")
    void login_ShouldThrowNotFound_WhenPasswordDoesNotMatch() {
        // Arrange
        when(barberRepository.findByEmail(barberEmail)).thenReturn(Optional.of(mockBarber));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> {
            barberService.login(loginDTO);
        });
        verify(jwtTokenService, never()).generateToken((Customer) any());
    }

    @Test
    @DisplayName("create (Barber): Deve criar barbeiro, encodar senha e salvar sem imagem")
    void create_ShouldCreateBarber_AndEncodePassword_WithoutImage() throws IOException {
        // Arrange
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(barberRepository.save(any(Barber.class))).thenReturn(new Barber());

        // Act
        barberService.create(createBarberDTO, null); // Sem arquivo

        // Assert
        verify(barberRepository, times(1)).save(barberCaptor.capture());
        Barber savedBarber = barberCaptor.getValue();
        assertEquals(hashedPassword, savedBarber.getPassword());
        assertFalse(savedBarber.isOwner());
        assertNull(savedBarber.getBarbershop());

        verify(storageService, never()).uploadFile(any(), any());
    }

    @Test
    @DisplayName("create (Barber): Deve criar barbeiro e fazer upload da imagem")
    void create_ShouldCreateBarber_AndUploadImage() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(storageService.uploadFile(mockFile, "barber-profiles")).thenReturn(mockUploadResult);
        // Captura o barbeiro salvo *duas vezes*
        when(barberRepository.save(any(Barber.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        barberService.create(createBarberDTO, mockFile);

        // Assert
        verify(storageService, times(1)).uploadFile(mockFile, "barber-profiles");
        // Verifica o save final
        verify(barberRepository, times(2)).save(barberCaptor.capture());
        Barber finalBarber = barberCaptor.getValue();
        assertEquals(imageUrl, finalBarber.getImageUrl());
        assertEquals(imagePublicId, finalBarber.getImageUrlPublicId());
    }

    @Test
    @DisplayName("delete (Barber): Deve publicar evento BeforeDeleteBarber")
    void delete_ShouldPublishEvent_AndDeleteBarber() {
        // Arrange
        when(barberRepository.findByEmail(barberEmail)).thenReturn(Optional.of(mockBarber));
        doNothing().when(publisher).publishEvent(any(BeforeDeleteBarber.class));
        doNothing().when(barberRepository).delete(mockBarber);

        // Act
        barberService.delete(barberEmail);

        // Assert
        verify(publisher, times(1)).publishEvent(any(BeforeDeleteBarber.class));
        verify(barberRepository, times(1)).delete(mockBarber);
    }

    // --- Fluxo de Gestão de Dono (Owner) ---

    @Test
    @DisplayName("createBarbershop: Deve criar loja, fazer upload de logo e setar dono")
    void createBarbershop_ShouldCreateShop_UploadLogo_AndSetOwner() throws IOException {
        // Arrange
        Barber futureOwner = new Barber(); // Um barbeiro sem loja
        futureOwner.setId(ownerId);
        futureOwner.setEmail(ownerEmail);

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(futureOwner));
        when(barbershopMapper.toEntity(createBarbershopDTO)).thenReturn(mockBarbershop);
        when(storageService.uploadFile(mockFile, "barbershop-logos")).thenReturn(mockUploadResult);
        when(barbershopRepository.save(any(Barbershop.class))).thenReturn(mockBarbershop);
        when(barberRepository.save(any(Barber.class))).thenReturn(futureOwner); // Salva o barbeiro atualizado

        // Act
        barberService.createBarbershop(ownerEmail, createBarbershopDTO, mockFile);

        // Assert
        verify(storageService, times(1)).uploadFile(mockFile, "barbershop-logos");
        verify(barbershopRepository, times(1)).save(barbershopCaptor.capture());
        Barbershop savedShop = barbershopCaptor.getValue();
        assertEquals(imageUrl, savedShop.getLogoUrl());

        verify(barberRepository, times(1)).save(barberCaptor.capture());
        Barber savedOwner = barberCaptor.getValue();
        assertTrue(savedOwner.isOwner());
        assertEquals(mockBarbershop.getId(), savedOwner.getBarbershop().getId());
    }

    @Test
    @DisplayName("createBarbershop: Deve lançar ReferenceException se barbeiro já for dono")
    void createBarbershop_ShouldThrowReferenceException_WhenAlreadyOwner() {
        // Arrange
        // mockOwner (do setup) já é dono e tem loja
        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner));

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            barberService.createBarbershop(ownerEmail, createBarbershopDTO, null);
        });
    }

    @Test
    @DisplayName("closeBarbershop: Deve fechar a loja com sucesso")
    void closeBarbershop_ShouldSucceed_WhenPasswordMatches_AndNoPendingAppointments() {
        // Arrange
        CloseBarbershopRequestDTO closeRequest = new CloseBarbershopRequestDTO();
        closeRequest.setPassword(rawPassword);

        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        // Sem agendamentos pendentes
        when(appointmentsRepository.existsByBarbershopIdAndStatus(barbershopId, AppointmentStatus.SCHEDULED)).thenReturn(false);
        // Encontra o staff (mockBarber) e o dono (mockOwner)
        when(barberRepository.findByBarbershopId(barbershopId)).thenReturn(List.of(mockBarber, mockOwner));

        // Act
        barberService.closeBarbershop(ownerEmail, closeRequest);

        // Assert
        // Verifica que o método save foi chamado duas vezes: uma para o staff e uma para o dono.
        verify(barberRepository, times(2)).save(barberCaptor.capture());
        List<Barber> savedBarbers = barberCaptor.getAllValues();

        // Encontra o staff e o dono na lista de barbeiros salvos pelo ID, pois o 'isOwner' do dono é setado para false antes de salvar.
        Barber savedStaff = savedBarbers.stream().filter(b -> b.getId().equals(barberId)).findFirst().orElseThrow();
        Barber savedOwner = savedBarbers.stream().filter(b -> b.getId().equals(ownerId)).findFirst().orElseThrow();

        assertNull(savedStaff.getBarbershop());
        assertTrue(savedStaff.getActivities().isEmpty());
        assertNull(savedOwner.getBarbershop());
        assertFalse(savedOwner.isOwner());

        // Verifica se as atividades foram deletadas
        verify(activityRepository, times(1)).deleteAll(anyList());
        // Verifica se a loja foi deletada
        verify(barbershopRepository, times(1)).delete(mockBarbershop);
    }

    @Test
    @DisplayName("closeBarbershop: Deve lançar ReferenceException se senha estiver errada")
    void closeBarbershop_ShouldThrowReferenceException_WhenPasswordFails() {
        // Arrange
        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner));
        when(passwordEncoder.matches(any(), eq(hashedPassword))).thenReturn(false);

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            barberService.closeBarbershop(ownerEmail, new CloseBarbershopRequestDTO());
        });
    }

    @Test
    @DisplayName("closeBarbershop: Deve lançar ReferenceException se houver agendamentos")
    void closeBarbershop_ShouldThrowReferenceException_WhenPendingAppointmentsExist() {
        // Arrange
        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner));
        when(passwordEncoder.matches(any(), eq(hashedPassword))).thenReturn(true);
        // Agendamentos pendentes!
        when(appointmentsRepository.existsByBarbershopIdAndStatus(barbershopId, AppointmentStatus.SCHEDULED)).thenReturn(true);

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            barberService.closeBarbershop(ownerEmail, new CloseBarbershopRequestDTO());
        });
    }

    @Test
    @DisplayName("deleteActivity: Deve lançar ReferenceException se houver agendamentos")
    void deleteActivity_ShouldThrowReferenceException_WhenPendingAppointmentsExist() {
        // Arrange
        // Helper getBarbershopFromOwner
        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner));
        // Lógica do método
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(mockActivity));
        when(appointmentsRepository.existsByActivitiesIdAndStatus(activityId, AppointmentStatus.SCHEDULED)).thenReturn(true);

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            barberService.deleteActivity(ownerEmail, activityId);
        });
        verify(activityRepository, never()).delete(any());
    }

    // --- Fluxo de Gestão de Vínculos (Staff) ---

    @Test
    @DisplayName("requestToJoinBarbershop: Deve criar pedido com sucesso")
    void requestToJoinBarbershop_ShouldCreatePendingRequest() {
        // Arrange
        Barber freeBarber = new Barber(); // Sem loja
        freeBarber.setId(barberId);
        when(barberRepository.findByEmail(barberEmail)).thenReturn(Optional.of(freeBarber));
        when(barbershopRepository.findByCnpj(cnpj)).thenReturn(Optional.of(mockBarbershop));
        when(joinRequestRepository.findByBarberIdAndBarbershopId(barberId, barbershopId)).thenReturn(Optional.empty());

        // Act
        barberService.requestToJoinBarbershop(barberEmail, cnpj);

        // Assert
        verify(joinRequestRepository, times(1)).save(any(BarbershopJoinRequest.class));
    }

    @Test
    @DisplayName("approveJoinRequest: Deve aprovar pedido e vincular barbeiro")
    void approveJoinRequest_ShouldApprove_AndLinkBarber() {
        // Arrange
        Barber freeBarber = new Barber(); // Sem loja
        freeBarber.setId(barberId);

        BarbershopJoinRequest request = new BarbershopJoinRequest();
        request.setId(1L);
        request.setBarber(freeBarber);
        request.setBarbershop(mockBarbershop);
        request.setStatus(JoinRequestStatus.PENDING);

        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner));
        when(joinRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        // Act
        barberService.approveJoinRequest(ownerEmail, 1L);

        // Assert
        verify(barberRepository, times(1)).save(barberCaptor.capture());
        Barber linkedBarber = barberCaptor.getValue();
        assertEquals(barbershopId, linkedBarber.getBarbershop().getId());

        verify(joinRequestRepository, times(1)).delete(request);
    }

    @Test
    @DisplayName("approveJoinRequest: Deve lançar ReferenceException se barbeiro já se vinculou")
    void approveJoinRequest_ShouldThrowReference_WhenBarberIsAlreadyLinked() {
        // Arrange
        mockBarber.setBarbershop(new Barbershop()); // Barbeiro já está em uma loja

        BarbershopJoinRequest request = new BarbershopJoinRequest();
        request.setId(1L);
        request.setBarber(mockBarber);
        request.setBarbershop(mockBarbershop);
        request.setStatus(JoinRequestStatus.PENDING);

        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner));
        when(joinRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            barberService.approveJoinRequest(ownerEmail, 1L);
        });
    }

    @Test
    @DisplayName("freeBarber: Deve lançar ReferenceException se for o Dono")
    void freeBarber_ShouldThrowReferenceException_WhenUserIsOwner() {
        // Arrange
        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner));

        // Act & Assert
        assertThrows(ReferenceException.class, () -> {
            barberService.freeBarber(ownerEmail);
        });
    }

    @Test
    @DisplayName("freeBarber: Deve desvincular barbeiro (staff) com sucesso")
    void freeBarber_ShouldSucceed_WhenUserIsStaff() {
        // Arrange
        mockBarber.setActivities(new HashSet<>(Set.of(mockActivity))); // Garante que a coleção é mutável
        when(barberRepository.findByEmail(barberEmail)).thenReturn(Optional.of(mockBarber));

        // Act
        barberService.freeBarber(barberEmail);

        // Assert
        verify(barberRepository, times(1)).save(barberCaptor.capture());
        Barber freedBarber = barberCaptor.getValue();
        assertNull(freedBarber.getBarbershop());
        assertTrue(freedBarber.getActivities().isEmpty());
    }

    // --- Fluxo de Gestão de Imagens (Uploads) ---

    @Test
    @DisplayName("updateBarbershopLogo: Deve deletar logo antigo e salvar novo")
    void updateBarbershopLogo_ShouldDeleteOld_AndUploadNew() throws IOException {
        // Arrange
        mockBarbershop.setLogoUrl(imageUrl);
        mockBarbershop.setLogoUrlPublicId(imagePublicId); // Tem logo antigo
        MultipartFile mockFile = mock(MultipartFile.class);
        UploadResultDTO newUpload = new UploadResultDTO("new_id", "new_url");

        when(barberRepository.findByEmail(ownerEmail)).thenReturn(Optional.of(mockOwner)); // Valida dono
        doNothing().when(storageService).deleteFile(imagePublicId);
        when(storageService.uploadFile(mockFile, "barbershop-logos")).thenReturn(newUpload);

        // Act
        barberService.updateBarbershopLogo(ownerEmail, mockFile);

        // Assert
        verify(storageService, times(1)).deleteFile(imagePublicId); // Deletou antigo
        verify(storageService, times(1)).uploadFile(mockFile, "barbershop-logos"); // Subiu novo
        verify(barbershopRepository, times(1)).save(barbershopCaptor.capture());
        assertEquals("new_url", barbershopCaptor.getValue().getLogoUrl());
        assertEquals("new_id", barbershopCaptor.getValue().getLogoUrlPublicId());
    }

    // --- Fluxo de Disponibilidade (Availability) ---

    @Test
    @DisplayName("getAvailableSlots: Deve retornar lista vazia se expediente não definido")
    void getAvailableSlots_ShouldReturnEmpty_WhenWorkHoursAreNull() {
        // Arrange
        mockBarber.setWorkStartTime(null);
        mockBarber.setWorkEndTime(null);
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(mockBarber));

        // Act
        List<LocalTime> slots = barberService.getAvailableSlots(barberId, LocalDate.now(), 30);

        // Assert
        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("getAvailableSlots: Deve retornar expediente completo se não houver agendamentos")
    void getAvailableSlots_ShouldReturnFullDay_WhenNoAppointments() {
        // Arrange
        mockBarber.setWorkStartTime(LocalTime.of(9, 0));
        mockBarber.setWorkEndTime(LocalTime.of(10, 0)); // 1h de expediente
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(mockBarber));
        when(appointmentsRepository.findByBarberIdAndStartTimeBetween(eq(barberId), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        // slotIntervalMinutes = 15 (do setUp)
        List<LocalTime> slots = barberService.getAvailableSlots(barberId, LocalDate.now(), 30); // Duração de 30 min

        // Assert
        // 9:00 (fim 9:30)
        // 9:15 (fim 9:45)
        // 9:30 (fim 10:00)
        // 9:45 (fim 10:15) -> estoura
        assertEquals(3, slots.size());
        assertEquals(LocalTime.of(9, 0), slots.get(0));
        assertEquals(LocalTime.of(9, 15), slots.get(1));
        assertEquals(LocalTime.of(9, 30), slots.get(2));
    }

    @Test
    @DisplayName("getAvailableSlots: Deve pular blocos com agendamentos")
    void getAvailableSlots_ShouldSkipBookedSlots() {
        // Arrange
        mockBarber.setWorkStartTime(LocalTime.of(9, 0));
        mockBarber.setWorkEndTime(LocalTime.of(11, 0)); // 2h de expediente
        when(barberRepository.findById(barberId)).thenReturn(Optional.of(mockBarber));

        // Agendamento das 9:30 às 10:00
        Appointments booked = new Appointments();
        booked.setStatus(AppointmentStatus.SCHEDULED);
        booked.setStartTime(LocalDate.now().atTime(9, 30).atOffset(ZoneOffset.UTC));
        booked.setEndTime(LocalDate.now().atTime(10, 0).atOffset(ZoneOffset.UTC));

        when(appointmentsRepository.findByBarberIdAndStartTimeBetween(eq(barberId), any(), any()))
                .thenReturn(List.of(booked));

        // Act
        List<LocalTime> slots = barberService.getAvailableSlots(barberId, LocalDate.now(), 30); // Duração de 30 min

        // Assert
        // 9:00 (fim 9:30) -> OK
        // 9:15 (fim 9:45) -> Conflita com 9:30
        // [Bloco Ocupado 9:30 - 10:00]
        // 10:00 (fim 10:30) -> OK
        // 10:15 (fim 10:45) -> OK
        // 10:30 (fim 11:00) -> OK
        // 10:45 (fim 11:15) -> Estoura
        assertEquals(4, slots.size());
        assertEquals(LocalTime.of(9, 0), slots.get(0));
        assertEquals(LocalTime.of(10, 0), slots.get(1));
        assertEquals(LocalTime.of(10, 15), slots.get(2));
        assertEquals(LocalTime.of(10, 30), slots.get(3));
    }
}
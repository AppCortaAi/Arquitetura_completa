package ifsp.edu.projeto.cortaai.service.impl;

import ifsp.edu.projeto.cortaai.model.Barber;
import ifsp.edu.projeto.cortaai.model.Customer;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para JwtTokenServiceImpl")
class JwtTokenServiceImplTest {

    @InjectMocks
    private JwtTokenServiceImpl jwtTokenService; // Classe sob teste

    // Chave secreta forte o suficiente para o algoritmo (HS256)
    private final String secretKeyString = "ChaveSecretaDeTesteSuperSeguraComPeloMenos256BitsDeTamanho";
    private final long expirationMs = 3600000; // 1 hora
    private final UUID customerId = UUID.randomUUID();
    private final UUID barberId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();

    private Customer mockCustomer;
    private Barber mockBarber;
    private Barber mockOwner;

    @BeforeEach
    void setUp() {
        // Injeta manualmente os valores que viriam do @Value
        ReflectionTestUtils.setField(jwtTokenService, "secretKeyString", secretKeyString);
        ReflectionTestUtils.setField(jwtTokenService, "expirationMs", expirationMs);

        // Configura entidades de mock
        mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setEmail("cliente@teste.com");

        mockBarber = new Barber();
        mockBarber.setId(barberId);
        mockBarber.setEmail("barbeiro@teste.com");
        mockBarber.setOwner(false); // Staff

        mockOwner = new Barber();
        mockOwner.setId(ownerId);
        mockOwner.setEmail("dono@teste.com");
        mockOwner.setOwner(true); // Dono
    }

    @Test
    @DisplayName("Deve gerar token para Cliente e validar as claims (Round-trip)")
    void generateToken_ForCustomer_ShouldCreateAndValidateClaims() {
        // Act (Geração)
        String token = jwtTokenService.generateToken(mockCustomer);

        // Assert (Geração)
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Act (Validação)
        Claims claims = jwtTokenService.validateTokenAndGetClaims(token);

        // Assert (Validação)
        assertNotNull(claims);
        assertEquals(mockCustomer.getEmail(), claims.getSubject());
        assertEquals(customerId.toString(), claims.get("userId", String.class));
        assertEquals("CUSTOMER", claims.get("userType", String.class));
    }

    @Test
    @DisplayName("Deve gerar token para Barbeiro (Staff) e validar claims (Round-trip)")
    void generateToken_ForBarber_ShouldCreateAndValidateClaims() {
        // Act (Geração)
        String token = jwtTokenService.generateToken(mockBarber);

        // Assert (Geração)
        assertNotNull(token);

        // Act (Validação)
        Claims claims = jwtTokenService.validateTokenAndGetClaims(token);

        // Assert (Validação)
        assertNotNull(claims);
        assertEquals(mockBarber.getEmail(), claims.getSubject());
        assertEquals(barberId.toString(), claims.get("userId", String.class));
        assertEquals("BARBER", claims.get("userType", String.class));
        assertEquals(false, claims.get("isOwner", Boolean.class), "Barbeiro staff não deve ser 'isOwner'");
    }

    @Test
    @DisplayName("Deve gerar token para Barbeiro (Dono) e validar claim 'isOwner=true' (Round-trip)")
    void generateToken_ForOwner_ShouldCreateAndValidateOwnerClaim() {
        // Act (Geração)
        String token = jwtTokenService.generateToken(mockOwner);

        // Assert (Geração)
        assertNotNull(token);

        // Act (Validação)
        Claims claims = jwtTokenService.validateTokenAndGetClaims(token);

        // Assert (Validação)
        assertNotNull(claims);
        assertEquals(mockOwner.getEmail(), claims.getSubject());
        assertEquals(ownerId.toString(), claims.get("userId", String.class));
        assertEquals("BARBER", claims.get("userType", String.class));
        assertEquals(true, claims.get("isOwner", Boolean.class), "Barbeiro dono deve ter 'isOwner=true'");
    }

    @Test
    @DisplayName("validateTokenAndGetClaims: Deve retornar nulo para token malformado")
    void validateTokenAndGetClaims_ShouldReturnNull_ForMalformedToken() {
        // Arrange
        String malformedToken = "token.invalido.sem-assinatura";

        // Act
        Claims claims = jwtTokenService.validateTokenAndGetClaims(malformedToken);

        // Assert
        assertNull(claims);
    }

    @Test
    @DisplayName("validateTokenAndGetClaims: Deve retornar nulo para token com assinatura errada")
    void validateTokenAndGetClaims_ShouldReturnNull_ForWrongSignature() {
        // Arrange
        // Gera um token com a chave correta
        String token = jwtTokenService.generateToken(mockCustomer);

        // Altera a chave secreta no serviço para simular uma assinatura inválida
        String outraChave = "EstaChaveTambemEhSuperSeguraMasEhDiferenteDaOriginal";
        ReflectionTestUtils.setField(jwtTokenService, "secretKeyString", outraChave);

        // Act
        // Tenta validar o token (gerado com a chave antiga) usando a chave nova
        Claims claims = jwtTokenService.validateTokenAndGetClaims(token);

        // Assert
        assertNull(claims, "A validação deveria falhar e retornar nulo");
    }

    @Test
    @DisplayName("validateTokenAndGetClaims: Deve retornar nulo para token expirado")
    void validateTokenAndGetClaims_ShouldReturnNull_ForExpiredToken() throws InterruptedException {
        // Arrange
        // Configura o serviço para gerar tokens que expiram em 1 milissegundo
        ReflectionTestUtils.setField(jwtTokenService, "expirationMs", 1L);

        // Gera o token (que já está praticamente expirado)
        String expiredToken = jwtTokenService.generateToken(mockCustomer);

        // Espera 10ms para garantir que o token expirou
        Thread.sleep(10);

        // Act
        // Tenta validar o token expirado
        Claims claims = jwtTokenService.validateTokenAndGetClaims(expiredToken);

        // Assert
        assertNull(claims, "A validação deveria falhar para um token expirado");
    }
}
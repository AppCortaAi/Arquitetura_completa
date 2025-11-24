package ifsp.edu.projeto.cortaai.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for the official Alphanumeric CNPJValidator")
class CNPJValidatorTest {

    @InjectMocks
    private CNPJValidator cnpjValidator; // A classe a ser testada

    @Mock
    private ConstraintValidatorContext mockContext; // Contexto mockado

    @BeforeEach
    void setUp() {
        // Inicializa o validador antes de cada teste
        cnpjValidator = new CNPJValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "12ABC34501DE35",     // Alphanumeric valid (from official example)
            "1345C3A5000106",     // Alphanumeric valid (from official example)
            "R55231B3000757",     // Alphanumeric valid (from official example)
            "90.021.382/0001-22", // Traditional numeric valid with mask
            "90.024.778/0001-23"  // Traditional numeric valid with mask
    })
    @DisplayName("Should return TRUE for valid CNPJs (numeric and alphanumeric)")
    void isValid_ShouldReturnTrue_ForValidCNPJs(String validCnpj) {
        // Act
        boolean result = cnpjValidator.isValid(validCnpj, mockContext);

        // Assert
        assertTrue(result, "CNPJ " + validCnpj + " should be considered valid.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "R55231B3000700",     // Invalid: Wrong check digit
            "90.025.108/0001-01", // Invalid: Traditional numeric with wrong DV
            "1345c3A5000106",     // Invalid: Contains lowercase letters
            "90.024.420/0001A2",  // Invalid: Letter in check digit part
            "90.025.255/0001",    // Invalid: Wrong length (too short)
            "12ABC34501DE35A",    // Invalid: Wrong length (too long)
            "00.000.000/0000-00", // Invalid: All zeros is not allowed
            "00000000000000"      // Invalid: All zeros without mask
    })
    @DisplayName("Should return FALSE for invalid CNPJs (wrong digit, format, or length)")
    void isValid_ShouldReturnFalse_ForInvalidCNPJs(String invalidCnpj) {
        // Act
        boolean result = cnpjValidator.isValid(invalidCnpj, mockContext);

        // Assert
        assertFalse(result, "CNPJ " + invalidCnpj + " should be considered invalid.");
    }

    @Test
    @DisplayName("Should return TRUE for a null CNPJ")
    void isValid_ShouldReturnTrue_ForNullCNPJ() {
        // Act
        boolean result = cnpjValidator.isValid(null, mockContext);

        // Assert
        assertTrue(result, "A null CNPJ should be considered valid by this validator (use @NotNull for presence check).");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should return TRUE for an empty or blank CNPJ")
    void isValid_ShouldReturnTrue_ForEmptyOrBlankCNPJ(String emptyCnpj) {
        // Act
        boolean result = cnpjValidator.isValid(emptyCnpj, mockContext);

        // Assert
        assertTrue(result, "An empty or blank CNPJ should be considered valid (use @NotBlank for presence check).");
    }
}
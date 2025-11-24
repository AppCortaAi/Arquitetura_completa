package ifsp.edu.projeto.cortaai.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests for CPFValidator")
class CPFValidatorTest {

    @InjectMocks
    private CPFValidator cpfValidator;

    @Mock
    private ConstraintValidatorContext mockContext;

    @ParameterizedTest
    @ValueSource(strings = {
            "55131332028",      // CPF válido verificado
            "00903690098",      // CPF válido verificado
            "59396797065"       // CPF válido verificado
    })
    @DisplayName("Should return true for valid CPFs")
    void isValid_ShouldReturnTrue_ForValidCPFs(String validCpf) {
        assertTrue(cpfValidator.isValid(validCpf, mockContext), "CPF " + validCpf + " should be considered valid.");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "11111111111",      // Todos os dígitos iguais
            "222.222.222-22",   // Todos os dígitos iguais com máscara
            "12345678901",      // Dígitos verificadores inválidos
            "1234567890",       // Comprimento inválido (10 dígitos)
            "123456789012",     // Comprimento inválido (12 dígitos)
            "abcdefghijk"       // Contém letras
    })
    @DisplayName("Should return false for invalid CPFs")
    void isValid_ShouldReturnFalse_ForInvalidCPFs(String invalidCpf) {
        assertFalse(cpfValidator.isValid(invalidCpf, mockContext), "CPF " + invalidCpf + " should be considered invalid.");
    }

    @Test
    @DisplayName("Should return true for a null CPF")
    void isValid_ShouldReturnTrue_ForNullCPF() {
        assertTrue(cpfValidator.isValid(null, mockContext), "A null CPF should be considered valid by this validator.");
    }

    @Test
    @DisplayName("Should return true for an empty CPF")
    void isValid_ShouldReturnTrue_ForEmptyCPF() {
        assertTrue(cpfValidator.isValid("", mockContext), "An empty CPF should be considered valid by this validator.");
    }

    @Test
    @DisplayName("Should return true for a blank CPF")
    void isValid_ShouldReturnTrue_ForBlankCPF() {
        assertTrue(cpfValidator.isValid("   ", mockContext), "A blank CPF should be considered valid by this validator.");
    }

    @Test
    @DisplayName("Should return false for a CPF with incorrect check digits")
    void isValid_ShouldReturnFalse_ForIncorrectCheckDigits() {
        String invalidCpf = "11122233345";
        assertFalse(cpfValidator.isValid(invalidCpf, mockContext), "A CPF with incorrect check digits should be invalid.");
    }
}
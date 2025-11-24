package ifsp.edu.projeto.cortaai.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<CPF, String> {

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return true; // Deixa a verificação de nulidade para @NotNull/@NotBlank
        }

        // Remove todos os caracteres que não são dígitos
        final String unmaskedCpf = cpf.replaceAll("[^0-9]", "");

        // Verifica o tamanho e se todos os dígitos são iguais
        if (unmaskedCpf.length() != 11 || unmaskedCpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            // --- Cálculo do Primeiro Dígito Verificador ---
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Integer.parseInt(String.valueOf(unmaskedCpf.charAt(i))) * (10 - i);
            }

            int remainder = sum % 11;
            int firstDigit = (remainder < 2) ? 0 : 11 - remainder;

            // Valida o primeiro dígito
            if (firstDigit != Integer.parseInt(String.valueOf(unmaskedCpf.charAt(9)))) {
                return false;
            }

            // --- Cálculo do Segundo Dígito Verificador ---
            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Integer.parseInt(String.valueOf(unmaskedCpf.charAt(i))) * (11 - i);
            }

            remainder = sum % 11;
            int secondDigit = (remainder < 2) ? 0 : 11 - remainder;

            // Valida o segundo dígito e retorna o resultado final
            return secondDigit == Integer.parseInt(String.valueOf(unmaskedCpf.charAt(10)));

        } catch (NumberFormatException e) {
            // Caso algum caractere não seja um número após a limpeza (improvável, mas seguro)
            return false;
        }
    }
}
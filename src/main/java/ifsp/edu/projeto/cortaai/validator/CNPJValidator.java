package ifsp.edu.projeto.cortaai.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CNPJValidator implements ConstraintValidator<CNPJ, String> {

    private static final int TAMANHO_CNPJ_SEM_DV = 12;
    // Regex ajustada para NÃO fazer o toUpperCase antes da validação.
    // Ela agora valida o formato original da string.
    private static final String REGEX_FORMATACAO_VALIDA = "^[A-Z0-9]{14}$";
    private static final String CNPJ_TODO_ZERADO = "00000000000000";

    @Override
    public boolean isValid(String cnpj, ConstraintValidatorContext context) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return true;
        }

        // 1. Normalização: Apenas remove a máscara. NÃO converte para maiúsculas ainda.
        final String cnpjSemMascara = cnpj.replaceAll("[./-]", "");

        // 2. Validação de Formato: Verifica se o formato original (sem máscara) é válido.
        // Isso vai rejeitar CNPJs com letras minúsculas, como "1345c3A5000106".
        if (!cnpjSemMascara.matches(REGEX_FORMATACAO_VALIDA) || cnpjSemMascara.equals(CNPJ_TODO_ZERADO)) {
            return false;
        }

        // 3. Validação dos Dígitos Verificadores
        String baseCnpj = cnpjSemMascara.substring(0, TAMANHO_CNPJ_SEM_DV);
        String dvInformado = cnpjSemMascara.substring(TAMANHO_CNPJ_SEM_DV);

        String dvCalculado = calculaDV(baseCnpj);
        return dvCalculado.equals(dvInformado);
    }

    private String calculaDV(String baseCnpj) {
        String dv1 = String.valueOf(calculaDigito(baseCnpj));
        String dv2 = String.valueOf(calculaDigito(baseCnpj + dv1));
        return dv1 + dv2;
    }

    private int calculaDigito(String base) {
        final int[] pesos = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        final int valorBaseAscii = (int) '0';
        int soma = 0;

        for (int i = base.length() - 1; i >= 0; i--) {
            int valorCaracter = (int) base.charAt(i) - valorBaseAscii;
            soma += valorCaracter * pesos[pesos.length - base.length() + i];
        }

        int resto = soma % 11;

        return (resto < 2) ? 0 : 11 - resto;
    }
}
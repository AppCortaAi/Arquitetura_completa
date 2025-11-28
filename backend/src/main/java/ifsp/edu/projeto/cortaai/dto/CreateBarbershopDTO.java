package ifsp.edu.projeto.cortaai.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CNPJ;

@Getter
@Setter
public class CreateBarbershopDTO {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(min = 14, max = 14) // Validando tamanho do CNPJ
    @CNPJ
    private String cnpj;

    @Size(max = 255)
    private String address;

    private String logoUrl;

    private String bannerUrl;
}
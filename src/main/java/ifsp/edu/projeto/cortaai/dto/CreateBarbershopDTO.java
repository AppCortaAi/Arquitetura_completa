package ifsp.edu.projeto.cortaai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBarbershopDTO {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(min = 14, max = 14) // Validando tamanho do CNPJ
    private String cnpj;

    @Size(max = 255)
    private String address;

}
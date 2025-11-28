package ifsp.edu.projeto.cortaai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BarberJoinRequestDTO {

    @NotBlank
    private String cnpj;
}
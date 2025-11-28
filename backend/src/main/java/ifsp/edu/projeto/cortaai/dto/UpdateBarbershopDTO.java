package ifsp.edu.projeto.cortaai.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBarbershopDTO {

    @Size(max = 255) // Não obrigatório, mas se enviado, tem tamanho máximo
    private String name;

    @Size(max = 255) // Não obrigatório
    private String address;

    // O CNPJ não será editável, por ser um identificador único do negócio.
}
package ifsp.edu.projeto.cortaai.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BarbershopDTO {

    private UUID id;
    private String name;
    private String cnpj;
    private String address;

}
package ifsp.edu.projeto.cortaai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BarberDTO {

    private UUID id;

    @NotNull
    @Size(max = 70)
    private String name;

    @NotNull
    @Size(max = 11)
    private String tell;

    @NotNull
    @Size(max = 70)
    private String email;

    @NotNull
    @Size(max = 11)
    private String documentCPF;

    @NotNull
    private boolean isOwner;

    // ID da barbearia à qual ele está vinculado (pode ser nulo)
    private UUID barbershopId;

    // As skills (serviços) podem ser expostas em um endpoint separado
    // ou como uma lista de ServiceDTO aqui, se necessário.
}
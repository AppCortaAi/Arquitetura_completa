package ifsp.edu.projeto.cortaai.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentRequestDTO {

    @NotNull
    @Future // Agendamento deve ser no futuro
    private OffsetDateTime startTime;

    @NotNull
    private UUID barbershopId;

    @NotNull
    private UUID barberId;

    @NotNull
    private UUID customerId; // Em um cenário real, isso viria do usuário autenticado

    @NotEmpty
    private List<UUID> activityIds;

}
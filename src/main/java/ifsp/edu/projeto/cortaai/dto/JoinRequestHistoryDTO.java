package ifsp.edu.projeto.cortaai.dto;

import ifsp.edu.projeto.cortaai.model.enums.JoinRequestStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class JoinRequestHistoryDTO {
    private Long requestId;
    private JoinRequestStatus status;
    private UUID barbershopId;
    private String barbershopName;
}

package ifsp.edu.projeto.cortaai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinRequestDTO {
    private Long requestId;
    private BarberInfoDTO barber;
}
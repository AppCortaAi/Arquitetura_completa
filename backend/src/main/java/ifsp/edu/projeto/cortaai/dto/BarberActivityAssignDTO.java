package ifsp.edu.projeto.cortaai.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BarberActivityAssignDTO {

    @NotEmpty
    private List<UUID> activityIds;

}
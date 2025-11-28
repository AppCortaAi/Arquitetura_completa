package ifsp.edu.projeto.cortaai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateActivityDTO {

    @NotBlank
    private String activityName;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @Positive
    private Integer durationMinutes;

    private String imageUrl;

    // O barbershopId virá do usuário (Owner) autenticado
}
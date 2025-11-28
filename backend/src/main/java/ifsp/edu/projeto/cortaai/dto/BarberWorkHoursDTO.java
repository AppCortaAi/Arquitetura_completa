package ifsp.edu.projeto.cortaai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Getter
@Setter
public class BarberWorkHoursDTO {

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime workStartTime;

    @NotNull
    @JsonFormat(pattern = "HH:mm")
    private LocalTime workEndTime;
}
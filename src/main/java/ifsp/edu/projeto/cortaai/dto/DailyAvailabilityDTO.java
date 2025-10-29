package ifsp.edu.projeto.cortaai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class DailyAvailabilityDTO {
    private LocalDate date;
    private boolean hasAvailability;
}
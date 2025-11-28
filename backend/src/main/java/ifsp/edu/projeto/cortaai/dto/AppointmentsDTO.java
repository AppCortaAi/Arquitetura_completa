package ifsp.edu.projeto.cortaai.dto;

import ifsp.edu.projeto.cortaai.model.enums.AppointmentStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AppointmentsDTO {

    private Long id;

    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    private AppointmentStatus status;

    private UUID barbershopId;

    private UUID barberId;

    private UUID customerId;

    private List<UUID> activityIds;

}
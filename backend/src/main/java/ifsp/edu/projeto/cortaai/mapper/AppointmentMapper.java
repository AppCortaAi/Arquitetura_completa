package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.AppointmentsDTO;
import ifsp.edu.projeto.cortaai.model.Activity;
import ifsp.edu.projeto.cortaai.model.Appointments;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mapping(source = "barbershop.id", target = "barbershopId")
    @Mapping(source = "barber.id", target = "barberId")
    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "activities", target = "activityIds")
    AppointmentsDTO toDTO(Appointments appointments);

    // Método auxiliar para mapear Set<Activity> para List<UUID>
    default List<UUID> mapActivitiesToIds(Set<Activity> activities) {
        if (activities == null || activities.isEmpty()) {
            return List.of();
        }
        return activities.stream()
                .map(Activity::getId)
                .collect(Collectors.toList());
    }

    // Como na implementação original, NÃO há método toEntity,
    // pois a criação de Appointments é complexa e feita no serviço.
}
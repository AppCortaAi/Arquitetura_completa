package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.AppointmentsDTO;
import ifsp.edu.projeto.cortaai.model.Activity;
import ifsp.edu.projeto.cortaai.model.Appointments;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors; // Importado

@Component
public class AppointmentMapper {

    public AppointmentsDTO toDTO(Appointments appointments) {
        if (appointments == null) {
            return null;
        }
        AppointmentsDTO appointmentsDTO = new AppointmentsDTO();
        appointmentsDTO.setId(appointments.getId());
        appointmentsDTO.setStartTime(appointments.getStartTime());
        appointmentsDTO.setEndTime(appointments.getEndTime());
        appointmentsDTO.setStatus(appointments.getStatus());

        // Mapeia as entidades aninhadas para seus IDs
        if (appointments.getBarbershop() != null) {
            appointmentsDTO.setBarbershopId(appointments.getBarbershop().getId());
        }
        if (appointments.getBarber() != null) {
            appointmentsDTO.setBarberId(appointments.getBarber().getId());
        }
        if (appointments.getCustomer() != null) {
            appointmentsDTO.setCustomerId(appointments.getCustomer().getId());
        }

        // Mapeia o Set<Service> para uma List<UUID>
        if (appointments.getActivities() != null) {
            appointmentsDTO.setActivityIds(
                    appointments.getActivities().stream()
                            .map(Activity::getId)
                            .collect(Collectors.toList())
            );
        }

        return appointmentsDTO;
    }

    // O método toEntity foi removido.
    // A lógica de criação está no AppointmentsServiceImpl
    // pois requer validações e busca de múltiplas entidades.

}
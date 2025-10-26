package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.CreateActivityDTO;
import ifsp.edu.projeto.cortaai.dto.ActivityDTO;
import ifsp.edu.projeto.cortaai.model.Activity;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    public ActivityDTO toDTO(Activity service) {
        if (service == null) {
            return null;
        }
        ActivityDTO dto = new ActivityDTO();
        dto.setId(service.getId());
        dto.setActivityName(service.getActivityName());
        dto.setPrice(service.getPrice());
        dto.setDurationMinutes(service.getDurationMinutes());
        if (service.getBarbershop() != null) {
            dto.setBarbershopId(service.getBarbershop().getId());
        }
        return dto;
    }

    public Activity toEntity(CreateActivityDTO dto) {
        if (dto == null) {
            return null;
        }
        Activity service = new Activity();
        service.setActivityName(dto.getActivityName());
        service.setPrice(dto.getPrice());
        service.setDurationMinutes(dto.getDurationMinutes());
        // O vínculo com a Barbershop é feito no Service
        return service;
    }
}
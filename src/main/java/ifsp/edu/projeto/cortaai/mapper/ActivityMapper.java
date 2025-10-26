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
        dto.setImageUrl(service.getImageUrl()); // LINHA ADICIONADA
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
        // A imagem (imageUrl) não é definida na criação, mas em um endpoint de upload
        return service;
    }
}
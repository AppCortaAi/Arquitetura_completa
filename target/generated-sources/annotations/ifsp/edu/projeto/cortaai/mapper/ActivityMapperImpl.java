package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.ActivityDTO;
import ifsp.edu.projeto.cortaai.dto.CreateActivityDTO;
import ifsp.edu.projeto.cortaai.model.Activity;
import ifsp.edu.projeto.cortaai.model.Barbershop;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-22T15:36:58-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class ActivityMapperImpl implements ActivityMapper {

    @Override
    public ActivityDTO toDTO(Activity service) {
        if ( service == null ) {
            return null;
        }

        ActivityDTO activityDTO = new ActivityDTO();

        activityDTO.setBarbershopId( serviceBarbershopId( service ) );
        activityDTO.setId( service.getId() );
        activityDTO.setActivityName( service.getActivityName() );
        activityDTO.setPrice( service.getPrice() );
        activityDTO.setDurationMinutes( service.getDurationMinutes() );
        activityDTO.setImageUrl( service.getImageUrl() );

        return activityDTO;
    }

    @Override
    public Activity toEntity(CreateActivityDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Activity activity = new Activity();

        activity.setActivityName( dto.getActivityName() );
        activity.setPrice( dto.getPrice() );
        activity.setDurationMinutes( dto.getDurationMinutes() );

        return activity;
    }

    private UUID serviceBarbershopId(Activity activity) {
        if ( activity == null ) {
            return null;
        }
        Barbershop barbershop = activity.getBarbershop();
        if ( barbershop == null ) {
            return null;
        }
        UUID id = barbershop.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}

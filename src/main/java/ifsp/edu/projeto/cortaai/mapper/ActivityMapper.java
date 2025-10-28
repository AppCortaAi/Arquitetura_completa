package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.CreateActivityDTO;
import ifsp.edu.projeto.cortaai.dto.ActivityDTO;
import ifsp.edu.projeto.cortaai.model.Activity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    @Mapping(source = "barbershop.id", target = "barbershopId")
    ActivityDTO toDTO(Activity service);

    // Mapeia do DTO de *criação* para a entidade
    // Ignora a imagem (tratada pelo serviço) e a barbearia (definida pelo serviço)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "barbershop", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "imageUrlPublicId", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "barbers", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    Activity toEntity(CreateActivityDTO dto);
}
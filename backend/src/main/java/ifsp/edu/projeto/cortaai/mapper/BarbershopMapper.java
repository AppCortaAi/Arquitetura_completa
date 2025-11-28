package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.BarbershopDTO;
import ifsp.edu.projeto.cortaai.dto.CreateBarbershopDTO;
import ifsp.edu.projeto.cortaai.model.Barbershop;
import ifsp.edu.projeto.cortaai.model.BarbershopHighlight;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BarbershopMapper {

    // Ensina o MapStruct a mapear a lista de destaques
    @Mapping(source = "highlights", target = "highlightUrls")
    BarbershopDTO toDTO(Barbershop barbershop);

    // Mapeia do DTO de criação para a entidade
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "logoUrl", ignore = true)
    @Mapping(target = "logoUrlPublicId", ignore = true)
    @Mapping(target = "bannerUrl", ignore = true)
    @Mapping(target = "bannerUrlPublicId", ignore = true)
    @Mapping(target = "highlights", ignore = true)
    @Mapping(target = "dateCreated", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "barbers", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "appointments", ignore = true)
    @Mapping(target = "joinRequests", ignore = true)
    Barbershop toEntity(CreateBarbershopDTO dto);

    // Método auxiliar (default) que o MapStruct usará para o mapeamento
    default List<String> mapHighlights(Set<BarbershopHighlight> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return List.of(); // Retorna lista vazia em vez de nulo
        }
        return highlights.stream()
                .map(BarbershopHighlight::getImageUrl)
                .collect(Collectors.toList());
    }
}
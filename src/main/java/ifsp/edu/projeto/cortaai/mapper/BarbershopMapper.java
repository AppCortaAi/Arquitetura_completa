package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.BarbershopDTO;
import ifsp.edu.projeto.cortaai.dto.CreateBarbershopDTO;
import ifsp.edu.projeto.cortaai.model.Barbershop;
import ifsp.edu.projeto.cortaai.model.BarbershopHighlight; // IMPORT ADICIONADO
import org.springframework.stereotype.Component;

import java.util.stream.Collectors; // IMPORT ADICIONADO

@Component
public class BarbershopMapper {

    public BarbershopDTO toDTO(Barbershop barbershop) {
        if (barbershop == null) {
            return null;
        }
        BarbershopDTO dto = new BarbershopDTO();
        dto.setId(barbershop.getId());
        dto.setName(barbershop.getName());
        dto.setCnpj(barbershop.getCnpj());
        dto.setAddress(barbershop.getAddress());

        // CAMPOS ADICIONADOS
        dto.setLogoUrl(barbershop.getLogoUrl());
        dto.setBannerUrl(barbershop.getBannerUrl());

        // MAPEAMENTO DA GALERIA
        if (barbershop.getHighlights() != null) {
            dto.setHighlightUrls(barbershop.getHighlights().stream()
                    .map(BarbershopHighlight::getImageUrl)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public Barbershop toEntity(CreateBarbershopDTO dto) {
        if (dto == null) {
            return null;
        }
        Barbershop barbershop = new Barbershop();
        barbershop.setName(dto.getName());
        barbershop.setCnpj(dto.getCnpj());
        barbershop.setAddress(dto.getAddress());
        // Logo e Banner não são definidos na criação, mas em endpoints de upload
        return barbershop;
    }
}
package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.BarbershopDTO;
import ifsp.edu.projeto.cortaai.dto.CreateBarbershopDTO;
import ifsp.edu.projeto.cortaai.model.Barbershop;
import org.springframework.stereotype.Component;

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
        return barbershop;
    }
}
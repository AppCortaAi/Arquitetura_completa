package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.BarbershopDTO;
import ifsp.edu.projeto.cortaai.dto.CreateBarbershopDTO;
import ifsp.edu.projeto.cortaai.model.Barbershop;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-22T15:36:59-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class BarbershopMapperImpl implements BarbershopMapper {

    @Override
    public BarbershopDTO toDTO(Barbershop barbershop) {
        if ( barbershop == null ) {
            return null;
        }

        BarbershopDTO barbershopDTO = new BarbershopDTO();

        barbershopDTO.setHighlightUrls( mapHighlights( barbershop.getHighlights() ) );
        barbershopDTO.setId( barbershop.getId() );
        barbershopDTO.setName( barbershop.getName() );
        barbershopDTO.setCnpj( barbershop.getCnpj() );
        barbershopDTO.setAddress( barbershop.getAddress() );
        barbershopDTO.setLogoUrl( barbershop.getLogoUrl() );
        barbershopDTO.setBannerUrl( barbershop.getBannerUrl() );

        return barbershopDTO;
    }

    @Override
    public Barbershop toEntity(CreateBarbershopDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Barbershop barbershop = new Barbershop();

        barbershop.setName( dto.getName() );
        barbershop.setCnpj( dto.getCnpj() );
        barbershop.setAddress( dto.getAddress() );

        return barbershop;
    }
}

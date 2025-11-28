package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.BarberDTO;
import ifsp.edu.projeto.cortaai.model.Barber;
import ifsp.edu.projeto.cortaai.model.Barbershop;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-22T15:36:59-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class BarberMapperImpl implements BarberMapper {

    @Override
    public BarberDTO toDTO(Barber barber) {
        if ( barber == null ) {
            return null;
        }

        BarberDTO barberDTO = new BarberDTO();

        barberDTO.setBarbershopId( barberBarbershopId( barber ) );
        barberDTO.setId( barber.getId() );
        barberDTO.setWorkStartTime( barber.getWorkStartTime() );
        barberDTO.setWorkEndTime( barber.getWorkEndTime() );
        barberDTO.setName( barber.getName() );
        barberDTO.setTell( barber.getTell() );
        barberDTO.setEmail( barber.getEmail() );
        barberDTO.setDocumentCPF( barber.getDocumentCPF() );
        barberDTO.setOwner( barber.isOwner() );
        barberDTO.setImageUrl( barber.getImageUrl() );

        return barberDTO;
    }

    @Override
    public Barber toEntity(BarberDTO barberDTO) {
        if ( barberDTO == null ) {
            return null;
        }

        Barber barber = new Barber();

        barber.setId( barberDTO.getId() );
        barber.setName( barberDTO.getName() );
        barber.setTell( barberDTO.getTell() );
        barber.setEmail( barberDTO.getEmail() );
        barber.setDocumentCPF( barberDTO.getDocumentCPF() );
        barber.setOwner( barberDTO.isOwner() );
        barber.setWorkStartTime( barberDTO.getWorkStartTime() );
        barber.setWorkEndTime( barberDTO.getWorkEndTime() );
        barber.setImageUrl( barberDTO.getImageUrl() );

        return barber;
    }

    private UUID barberBarbershopId(Barber barber) {
        if ( barber == null ) {
            return null;
        }
        Barbershop barbershop = barber.getBarbershop();
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

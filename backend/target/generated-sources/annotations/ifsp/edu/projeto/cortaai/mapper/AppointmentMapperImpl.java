package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.AppointmentsDTO;
import ifsp.edu.projeto.cortaai.model.Appointments;
import ifsp.edu.projeto.cortaai.model.Barber;
import ifsp.edu.projeto.cortaai.model.Barbershop;
import ifsp.edu.projeto.cortaai.model.Customer;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-27T22:23:51-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class AppointmentMapperImpl implements AppointmentMapper {

    @Override
    public AppointmentsDTO toDTO(Appointments appointments) {
        if ( appointments == null ) {
            return null;
        }

        AppointmentsDTO appointmentsDTO = new AppointmentsDTO();

        appointmentsDTO.setBarbershopId( appointmentsBarbershopId( appointments ) );
        appointmentsDTO.setBarberId( appointmentsBarberId( appointments ) );
        appointmentsDTO.setCustomerId( appointmentsCustomerId( appointments ) );
        appointmentsDTO.setActivityIds( mapActivitiesToIds( appointments.getActivities() ) );
        appointmentsDTO.setId( appointments.getId() );
        appointmentsDTO.setStartTime( appointments.getStartTime() );
        appointmentsDTO.setEndTime( appointments.getEndTime() );
        appointmentsDTO.setStatus( appointments.getStatus() );

        return appointmentsDTO;
    }

    private UUID appointmentsBarbershopId(Appointments appointments) {
        if ( appointments == null ) {
            return null;
        }
        Barbershop barbershop = appointments.getBarbershop();
        if ( barbershop == null ) {
            return null;
        }
        UUID id = barbershop.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private UUID appointmentsBarberId(Appointments appointments) {
        if ( appointments == null ) {
            return null;
        }
        Barber barber = appointments.getBarber();
        if ( barber == null ) {
            return null;
        }
        UUID id = barber.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private UUID appointmentsCustomerId(Appointments appointments) {
        if ( appointments == null ) {
            return null;
        }
        Customer customer = appointments.getCustomer();
        if ( customer == null ) {
            return null;
        }
        UUID id = customer.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}

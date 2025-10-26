package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.BarberDTO;
import ifsp.edu.projeto.cortaai.model.Barber;
import org.springframework.stereotype.Component;
// O import do CreateBarberDTO foi removido

@Component
public class BarberMapper {

    public BarberDTO toDTO(Barber barber) {
        if (barber == null) {
            return null;
        }
        BarberDTO barberDTO = new BarberDTO();
        barberDTO.setId(barber.getId());
        barberDTO.setName(barber.getName());
        barberDTO.setTell(barber.getTell());
        barberDTO.setEmail(barber.getEmail());
        barberDTO.setDocumentCPF(barber.getDocumentCPF());
        barberDTO.setOwner(barber.isOwner());
        barberDTO.setWorkStartTime(barber.getWorkStartTime());
        barberDTO.setWorkEndTime(barber.getWorkEndTime());


        // Mapeia o objeto Barbershop para apenas seu ID
        if (barber.getBarbershop() != null) {
            barberDTO.setBarbershopId(barber.getBarbershop().getId());
        }

        return barberDTO;
    }

    public Barber toEntity(BarberDTO barberDTO) {
        if (barberDTO == null) {
            return null;
        }
        Barber barber = new Barber();
        // ID não é mapeado do DTO
        barber.setName(barberDTO.getName());
        barber.setTell(barberDTO.getTell());
        barber.setEmail(barberDTO.getEmail());
        barber.setDocumentCPF(barberDTO.getDocumentCPF());
        barber.setOwner(barberDTO.isOwner());

        // O vínculo da Barbershop (barbershopId) é tratado no Service, não aqui.

        return barber;
    }

    // O método toEntity(CreateBarberDTO) foi removido pois a lógica
    // de criação (com criptografia de senha) está no BarberServiceImpl.
}
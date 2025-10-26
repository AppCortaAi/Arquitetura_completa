package ifsp.edu.projeto.cortaai.service;

import ifsp.edu.projeto.cortaai.dto.AppointmentRequestDTO;
import ifsp.edu.projeto.cortaai.dto.AppointmentsDTO;
import java.util.List;
import java.util.UUID;

public interface AppointmentsService {

    List<AppointmentsDTO> findAll();

    AppointmentsDTO get(Long id);

    Long create(AppointmentRequestDTO appointmentsDTO);

    void update(Long id, AppointmentRequestDTO appointmentsDTO);

    void cancel(Long id); // Regra de negócio: "status deve ser atualizado (ex: CANCELLED)"

    List<AppointmentsDTO> findForBarbershop(UUID ownerId);

    List<AppointmentsDTO> findForBarber(UUID barberId);

    void delete(Long id); // Mantido caso a exclusão física ainda seja necessária
}
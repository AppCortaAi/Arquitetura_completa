package ifsp.edu.projeto.cortaai.service;

import ifsp.edu.projeto.cortaai.dto.AppointmentRequestDTO;
import ifsp.edu.projeto.cortaai.dto.AppointmentsDTO;
import ifsp.edu.projeto.cortaai.model.Appointments;

import java.util.List;
import java.util.UUID;

public interface AppointmentsService {

    List<AppointmentsDTO> findAll();

    AppointmentsDTO get(Long id);

    // ALTERADO: Adicionado String customerEmail
    Long create(AppointmentRequestDTO appointmentsDTO, String customerEmail);

    // ALTERADO: Adicionado String customerEmail
    void update(Long id, AppointmentRequestDTO appointmentsDTO, String customerEmail);

    // ALTERADO: Adicionado String userEmail (pode ser cliente ou dono)
    void cancel(Long id, String userEmail);

    // ALTERADO: de UUID ownerId para String ownerEmail
    List<AppointmentsDTO> findForBarbershop(String ownerEmail);

    List<AppointmentsDTO> findForCustomer(final String customerEmail);

    // ALTERADO: de UUID barberId para String barberEmail
    List<AppointmentsDTO> findForBarber(String barberEmail);

    // ALTERADO: Adicionado String userEmail
    void delete(Long id, String userEmail);




}


package ifsp.edu.projeto.cortaai.service;

import ifsp.edu.projeto.cortaai.dto.AppointmentRequestDTO;
import ifsp.edu.projeto.cortaai.dto.AppointmentsDTO;

import java.util.List;

public interface AppointmentsService {

    List<AppointmentsDTO> findAll();

    AppointmentsDTO get(Long id);

    Long create(AppointmentRequestDTO appointmentsDTO, String customerEmail);

    void update(Long id, AppointmentRequestDTO appointmentsDTO, String customerEmail);

    void cancel(Long id, String userEmail);

    List<AppointmentsDTO> findForBarbershop(String ownerEmail);

    List<AppointmentsDTO> findForCustomer(final String customerEmail);

    List<AppointmentsDTO> findForBarber(String barberEmail);

    void delete(Long id, String userEmail);

    void conclude(Long id, String barberEmail);
}


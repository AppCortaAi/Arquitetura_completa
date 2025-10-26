package ifsp.edu.projeto.cortaai.service;

import ifsp.edu.projeto.cortaai.dto.CustomerCreateDTO;
import ifsp.edu.projeto.cortaai.dto.CustomerDTO;
import ifsp.edu.projeto.cortaai.dto.LoginDTO;
import java.util.List;
import java.util.UUID;

public interface CustomerService {

    List<CustomerDTO> findAll();

    CustomerDTO get(UUID id);

    UUID create(CustomerCreateDTO customerCreateDTO);

    void update(UUID id, CustomerDTO customerDTO);

    void delete(UUID id);

    CustomerDTO login(LoginDTO loginDTO); // Assinatura mantida

    // --- Métodos de validação ---
    boolean tellExists(String tell);

    boolean emailExists(String email);

    boolean documentCPFExists(String documentCPF);

    // O método previousAppointmentExists foi removido
}
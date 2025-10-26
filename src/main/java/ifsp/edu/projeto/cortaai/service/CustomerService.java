package ifsp.edu.projeto.cortaai.service;

import ifsp.edu.projeto.cortaai.dto.CustomerCreateDTO;
import ifsp.edu.projeto.cortaai.dto.CustomerDTO;
import ifsp.edu.projeto.cortaai.dto.LoginDTO;
import org.springframework.web.multipart.MultipartFile; // IMPORT ADICIONADO

import java.io.IOException; // IMPORT ADICIONADO
import java.util.List;
import java.util.UUID;

public interface CustomerService {

    List<CustomerDTO> findAll();

    CustomerDTO get(UUID id);

    UUID create(CustomerCreateDTO customerCreateDTO);

    void update(UUID id, CustomerDTO customerDTO);

    void delete(UUID id);

    CustomerDTO login(LoginDTO loginDTO);

    // --- Métodos de validação ---
    boolean tellExists(String tell);

    boolean emailExists(String email);

    boolean documentCPFExists(String documentCPF);

    // NOVO MÉTODO
    String updateProfilePhoto(UUID customerId, MultipartFile file) throws IOException;

}
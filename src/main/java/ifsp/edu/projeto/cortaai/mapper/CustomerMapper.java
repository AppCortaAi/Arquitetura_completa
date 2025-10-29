package ifsp.edu.projeto.cortaai.mapper;

import ifsp.edu.projeto.cortaai.dto.CustomerDTO;
import ifsp.edu.projeto.cortaai.model.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring") // Define que é um mapper e o Spring deve gerenciá-lo
public interface CustomerMapper {

    CustomerDTO toDTO(Customer customer);

    Customer toEntity(CustomerDTO customerDTO);
}
package ifsp.edu.projeto.cortaai.service.impl;

import ifsp.edu.projeto.cortaai.dto.CustomerCreateDTO;
import ifsp.edu.projeto.cortaai.dto.CustomerDTO;
import ifsp.edu.projeto.cortaai.dto.LoginDTO;
import ifsp.edu.projeto.cortaai.events.BeforeDeleteCustomer;
import ifsp.edu.projeto.cortaai.exception.NotFoundException;
import ifsp.edu.projeto.cortaai.mapper.CustomerMapper;
import ifsp.edu.projeto.cortaai.model.Customer;
import ifsp.edu.projeto.cortaai.repository.CustomerRepository;
import ifsp.edu.projeto.cortaai.service.CustomerService;
import ifsp.edu.projeto.cortaai.service.StorageService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder; // IMPORTANTE
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher publisher;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService; // DEPENDÊNCIA ADICIONADA

    public CustomerServiceImpl(final CustomerRepository customerRepository,
                               final ApplicationEventPublisher publisher,
                               final CustomerMapper customerMapper,
                               final PasswordEncoder passwordEncoder,
                               final StorageService storageService) { // ADICIONADO AO CONSTRUTOR
        this.customerRepository = customerRepository;
        this.publisher = publisher;
        this.customerMapper = customerMapper;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService; // INJETADO
    }

    @Override
    public List<CustomerDTO> findAll() {
        final List<Customer> customers = customerRepository.findAll(Sort.by("id"));
        return customers.stream()
                .map(customerMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional // Garante que a transação seja apenas de leitura
    public CustomerDTO login(final LoginDTO loginDTO) {
        final Customer customer = customerRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new NotFoundException("Usuário ou senha inválidos"));

        // Compara a senha enviada com o hash salvo no banco
        if (!passwordEncoder.matches(loginDTO.getPassword(), customer.getPassword())) {
            throw new NotFoundException("Usuário ou senha inválidos");
        }

        return customerMapper.toDTO(customer);
    }

    @Override
    public CustomerDTO get(final UUID id) {
        return customerRepository.findById(id)
                .map(customerMapper::toDTO)
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public UUID create(final CustomerCreateDTO customerCreateDTO) {
        final Customer customer = new Customer();
        customer.setName(customerCreateDTO.getName());
        customer.setTell(customerCreateDTO.getTell());
        customer.setEmail(customerCreateDTO.getEmail());
        customer.setDocumentCPF(customerCreateDTO.getDocumentCPF());
        // Criptografa a senha antes de salvar
        customer.setPassword(passwordEncoder.encode(customerCreateDTO.getPassword()));
        return customerRepository.save(customer).getId();
    }

    @Override
    public void update(final UUID id, final CustomerDTO customerDTO) {
        final Customer customer = customerRepository.findById(id)
                .orElseThrow(NotFoundException::new);

        customer.setName(customerDTO.getName());
        customer.setTell(customerDTO.getTell());
        customer.setEmail(customerDTO.getEmail());
        customer.setDocumentCPF(customerDTO.getDocumentCPF());

        // A imagem é atualizada por outro método (updateProfilePhoto)
        // Mas se for enviado no DTO, podemos atualizar aqui também
        if(customerDTO.getImageUrl() != null) {
            customer.setImageUrl(customerDTO.getImageUrl());
        }

        customerRepository.save(customer);
    }

    @Override
    public void delete(final UUID id) {
        final Customer customer = customerRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        publisher.publishEvent(new BeforeDeleteCustomer(id));
        customerRepository.delete(customer);
    }

    // --- Métodos de validação ---

    @Override
    public boolean tellExists(final String tell) {
        return customerRepository.existsByTellIgnoreCase(tell);
    }

    @Override
    public boolean emailExists(final String email) {
        return customerRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean documentCPFExists(final String documentCPF) {
        return customerRepository.existsByDocumentCPFIgnoreCase(documentCPF);
    }

    @Override
    @Transactional
    public String updateProfilePhoto(UUID customerId, MultipartFile file) throws IOException {
        // 1. Verifica se o cliente existe
        final Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        // 2. Faz o upload do arquivo para o Cloudinary na pasta "customer-profiles"
        final String imageUrl = storageService.uploadFile(file, "customer-profiles");

        // 3. Salva a URL no banco de dados
        customer.setImageUrl(imageUrl);
        customerRepository.save(customer);

        return imageUrl;
    }
}
package ifsp.edu.projeto.cortaai.service;

import ifsp.edu.projeto.cortaai.model.Barber;
import ifsp.edu.projeto.cortaai.model.Customer;
import io.jsonwebtoken.Claims;

import java.util.UUID;

public interface JwtTokenService {
    // Sobrecarga para gerar token para Customer
    String generateToken(Customer customer);

    // Sobrecarga para gerar token para Barber
    String generateToken(Barber barber);

    // Método para validar e extrair as permissões (claims)
    Claims validateTokenAndGetClaims(String token);
}
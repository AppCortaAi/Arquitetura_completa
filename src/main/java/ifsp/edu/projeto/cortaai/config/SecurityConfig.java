package ifsp.edu.projeto.cortaai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // IMPORTAR ESTE
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // Permite acesso não autenticado aos seguintes endpoints:
                        .requestMatchers(
                                // Endpoints públicos da nossa API (criação e login)
                                "/api/customers/register",
                                "/api/customers/login",
                                "/api/barbers/register",
                                "/api/barbers/login"
                        ).permitAll()

                        // Exige autenticação para qualquer outra requisição (incluindo Swagger)
                        .anyRequest().authenticated()
                )
                // Habilita um formulário de login padrão para autenticação via navegador,
                // que será usado para acessar endpoints protegidos como o Swagger.
                .formLogin(withDefaults());
        return http.build();
    }
}
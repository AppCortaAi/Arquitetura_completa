package ifsp.edu.projeto.cortaai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // IMPORTAR ESTE
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User; // NOVO IMPORT
import org.springframework.security.core.userdetails.UserDetails; // NOVO IMPORT
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager; // NOVO IMPORT
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * NOVO BEAN: Define um usuário "em memória" para fins de administração/swagger.
     * Este usuário é separado dos seus usuários 'Customer' ou 'Barber' do banco de dados.
     */
    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.builder()
                .username("admin") // Você pode mudar o usuário aqui
                .password(passwordEncoder.encode("AdminCortaai@2112")) // Você pode mudar a senha aqui
                .roles("ADMIN") // Define a "permissão" deste usuário
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // 1. Permite acesso PÚBLICO aos endpoints de registro e login da API
                        .requestMatchers(
                                "/api/customers/register",
                                "/api/customers/login",
                                "/api/barbers/register",
                                "/api/barbers/login"
                        ).permitAll()

                        // 2. Protege o Swagger e TODO O RESTANTE DA API com a role "ADMIN"
                        .requestMatchers(
                                "/", // Raiz (que redireciona pro Swagger)
                                "/swagger-ui/**", // Interface do Swagger
                                "/v3/api-docs/**",  // Definição da API (JSON)
                                "/api/**" // Todos os outros endpoints da API
                        ).hasRole("ADMIN")

                        // 3. Qualquer outra requisição não mapeada
                        .anyRequest().authenticated()
                )
                // Substitui o .formLogin() por .httpBasic().
                // Isso mostrará um pop-up de login no navegador em vez de um formulário HTML.
                .httpBasic(withDefaults());
        return http.build();
    }
}
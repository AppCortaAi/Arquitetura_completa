package ifsp.edu.projeto.cortaai.exception;

import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.CONFLICT)
@Getter
@Setter
public class ReferenceException extends RuntimeException {

    private String key = null;
    private ArrayList<Object> params = new ArrayList<>();

    public ReferenceException() {
        // Construtor vazio para permitir a instanciação e configuração posterior.
    }

    public ReferenceException(String key) {
        super(key); // Adiciona a chave como a mensagem da exceção pai
        this.key = key;
    }

    public void addParam(final Object param) {
        params.add(param);
    }

    @Override
    public String getMessage() {
        // ALTERADO:
        // Retorna a 'key' (que foi passada para super(key) no construtor)
        // A biblioteca 'error-handling' irá serializar 'key' e 'params'
        // separadamente no JSON de resposta.
        return this.key;
    }

}
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
        this.key = key;
    }

    public void addParam(final Object param) {
        params.add(param);
    }

    @Override
    public String getMessage() {
        String message = key;
        if (!params.isEmpty()) {
            message += "," + params.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        }
        return message;
    }

}

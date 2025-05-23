package crmApp.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;


@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientException extends RuntimeException {
    HttpStatus httpStatus;
    public ClientException(HttpStatus status, String message) {
        super(message);
        this.httpStatus=status;
    }

}

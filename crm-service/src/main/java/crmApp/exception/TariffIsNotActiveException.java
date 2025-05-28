package crmApp.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class TariffIsNotActiveException extends RuntimeException {
    public TariffIsNotActiveException(String message) {
        super(message);
    }
}

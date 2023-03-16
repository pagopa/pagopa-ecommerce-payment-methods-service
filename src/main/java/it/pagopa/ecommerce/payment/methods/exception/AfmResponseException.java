package it.pagopa.ecommerce.payment.methods.exception;

import org.springframework.http.HttpStatus;

public class AfmResponseException extends RuntimeException {
    public HttpStatus status;
    public String reason;

    public AfmResponseException(
            HttpStatus statusCode,
            String reason
    ) {
        this.reason = reason;
        this.status = statusCode;
    }
}

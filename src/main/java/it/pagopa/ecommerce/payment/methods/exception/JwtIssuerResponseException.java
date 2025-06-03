package it.pagopa.ecommerce.payment.methods.exception;

import org.springframework.http.HttpStatus;

public class JwtIssuerResponseException extends RuntimeException {
    public HttpStatus status;
    public String reason;

    public JwtIssuerResponseException(
            HttpStatus statusCode,
            String reason
    ) {
        this.reason = reason;
        this.status = statusCode;
    }
}

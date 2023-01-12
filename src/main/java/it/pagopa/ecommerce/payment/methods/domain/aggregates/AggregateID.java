package it.pagopa.ecommerce.payment.methods.domain.aggregates;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

@Target(METHOD)
@Documented
public @interface AggregateID {

}

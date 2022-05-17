package it.pagopa.pspmatcher.domain.aggregates;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Target(TYPE)
@Documented
public @interface Aggregate {

}
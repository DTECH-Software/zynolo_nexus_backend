package com.zynolo_nexus.auth_service.validator.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.zynolo_nexus.auth_service.validator.validators.ValidPasswordValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidPasswordValidator.class)
@Documented
public @interface ValidPassword {

    String message() default "Password must contain 8+ chars with upper, lower, digit, and symbol";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

package com.irg.ftpserver.validation;

import com.irg.ftpserver.validation.validator.IPaddressValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IPaddressValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIpAddress {
    String message() default "Invalid IP Address";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

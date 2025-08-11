package org.example.authservice.anotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.authservice.validator.UniquePhoneNumberValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniquePhoneNumberValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniquePhoneNumber {
    String message() default "Номер телефону вже використовується";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

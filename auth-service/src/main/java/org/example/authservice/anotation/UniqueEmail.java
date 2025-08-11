package org.example.authservice.anotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.authservice.validator.UniqueEmailValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {
    String message() default "Електронна пошта вже використовується";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

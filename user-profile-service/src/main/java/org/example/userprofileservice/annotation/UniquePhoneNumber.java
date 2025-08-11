package org.example.userprofileservice.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.example.userprofileservice.validator.UniquePhoneNumberValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniquePhoneNumberValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniquePhoneNumber {
    String message() default "Цей номер телефону вже використовується";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}


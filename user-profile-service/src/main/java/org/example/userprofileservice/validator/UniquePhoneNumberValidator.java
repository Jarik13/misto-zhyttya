package org.example.userprofileservice.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.example.userprofileservice.annotation.UniquePhoneNumber;
import org.example.userprofileservice.repository.UserProfileRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniquePhoneNumberValidator implements ConstraintValidator<UniquePhoneNumber, String> {
    private final UserProfileRepository userProfileRepository;

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return true;
        }
        return !userProfileRepository.existsByPhoneNumber(phoneNumber);
    }
}


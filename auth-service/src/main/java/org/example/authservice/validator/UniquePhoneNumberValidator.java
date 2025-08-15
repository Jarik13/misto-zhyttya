package org.example.authservice.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.example.authservice.anotation.UniquePhoneNumber;
import org.example.authservice.grpc.UserProfileServiceGrpcClient;
import org.springframework.stereotype.Component;
import user.profile.CheckPhoneNumberRequest;

@Component
@RequiredArgsConstructor
public class UniquePhoneNumberValidator implements ConstraintValidator<UniquePhoneNumber, String> {
    private final UserProfileServiceGrpcClient userProfileServiceGrpcClient;

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return true;
        }

        CheckPhoneNumberRequest request = CheckPhoneNumberRequest.newBuilder()
                .setPhoneNumber(phoneNumber)
                .build();

        return userProfileServiceGrpcClient.checkPhoneNumber(request).getIsUnique();
    }
}

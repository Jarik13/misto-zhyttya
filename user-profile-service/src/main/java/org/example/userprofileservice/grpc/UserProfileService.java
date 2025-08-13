package org.example.userprofileservice.grpc;

import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.userprofileservice.dto.profile.ProfileRequest;
import org.example.userprofileservice.dto.profile.ProfileResponse;
import org.example.userprofileservice.exception.ValidationException;
import org.example.userprofileservice.kafka.UserProfileProducer;
import org.example.userprofileservice.mapper.UserProfileMapper;
import org.example.userprofileservice.model.Gender;
import org.example.userprofileservice.model.Profile;
import org.example.userprofileservice.repository.UserProfileRepository;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.BeanPropertyBindingResult;
import user.profile.*;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserProfileService extends UserProfileServiceGrpc.UserProfileServiceImplBase {
    private final UserProfileRepository userProfileRepository;
    private final UserProfileProducer userProfileProducer;

    @Override
    @Transactional
    public void createUserProfile(CreateUserProfileRequest request,
                                  StreamObserver<CreateUserProfileResponse> responseObserver) {
        log.info("createUserProfile received request: {}", request);

        Profile profile = userProfileRepository.findByUserId(request.getUserId())
                .orElseGet(() -> userProfileRepository.save(UserProfileMapper.toUserProfile(request)));

        CreateUserProfileResponse response = CreateUserProfileResponse.newBuilder()
                .setUsername(profile.getUsername())
                .setAvatarKey(profile.getAvatarKey())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void checkPhoneNumber(CheckPhoneNumberRequest request,
                                 StreamObserver<CheckPhoneNumberResponse> responseObserver) {
        log.info("checkPhoneNumber received request: {}", request);

        CheckPhoneNumberResponse response = CheckPhoneNumberResponse.newBuilder()
                .setIsUnique(!userProfileRepository.existsByPhoneNumber(request.getPhoneNumber()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserProfileInfo(GetUserProfileInfoRequest request,
                                   StreamObserver<GetUserProfileInfoResponse> responseObserver) {
        log.info("getUserProfileInfo received request: {}", request);

        ProfileResponse profile = findUserProfile(request.getUserId());

        GetUserProfileInfoResponse response = GetUserProfileInfoResponse.newBuilder()
                .setUsername(profile.username())
                .setAvatarKey(profile.avatarKey())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public ProfileResponse findUserProfile(String userId) {
        Profile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

        return UserProfileMapper.toUserProfileResponse(profile);
    }

    @Transactional
    public void updateUserProfile(String userId, ProfileRequest request) {
        log.info("updateUserProfile received request: {}", request);

        Profile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

        if (userProfileRepository.existsByPhoneNumberAndUserIdNot(request.phoneNumber(), userId)) {
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "profileRequest");
            bindingResult.rejectValue("phoneNumber", "VALIDATION_ERROR", "Цей номер телефону вже використовується");
            throw new ValidationException(bindingResult);
        }

        profile.setUsername(request.username());
        profile.setPhoneNumber(request.phoneNumber());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setGender(Gender.fromId(request.genderId()));

        String oldAvatarKey = profile.getAvatarKey();
        profile.setAvatarKey(request.avatarKey());

        userProfileRepository.save(profile);

        if (oldAvatarKey != null && !oldAvatarKey.equals(request.avatarKey())) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    userProfileProducer.deleteUserAvatar(oldAvatarKey);
                }
            });
        }
    }
}

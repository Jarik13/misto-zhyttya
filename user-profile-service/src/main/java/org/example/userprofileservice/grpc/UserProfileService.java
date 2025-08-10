package org.example.userprofileservice.grpc;

import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.userprofileservice.dto.profile.ProfileResponse;
import org.example.userprofileservice.mapper.UserProfileMapper;
import org.example.userprofileservice.model.Profile;
import org.example.userprofileservice.repository.UserProfileRepository;
import user.profile.UserProfileServiceGrpc;
import user.profile.CreateUserProfileRequest;
import user.profile.CreateUserProfileResponse;
import user.profile.CheckPhoneNumberRequest;
import user.profile.CheckPhoneNumberResponse;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserProfileService extends UserProfileServiceGrpc.UserProfileServiceImplBase {
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public void createUserProfile(CreateUserProfileRequest request,
                                  StreamObserver<CreateUserProfileResponse> responseObserver) {
        log.info("createUserProfile received request: {}", request);

        Profile profile = userProfileRepository.findByUserId(request.getUserId())
                .orElseGet(() -> userProfileRepository.save(UserProfileMapper.toUserProfile(request)));

        CreateUserProfileResponse response = CreateUserProfileResponse.newBuilder()
                .setProfileId(String.valueOf(profile.getId()))
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

    public ProfileResponse findUserProfile(String userId) {
        Profile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

        return UserProfileMapper.toUserProfileResponse(profile);
    }
}

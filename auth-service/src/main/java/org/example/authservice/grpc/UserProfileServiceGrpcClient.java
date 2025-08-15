package org.example.authservice.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import user.profile.*;

@Slf4j
@Service
public class UserProfileServiceGrpcClient {
    private final UserProfileServiceGrpc.UserProfileServiceBlockingStub blockingStub;

    public UserProfileServiceGrpcClient(
            @Value("${user-profile.service.address}") String serverAddress,
            @Value("${user-profile.service.port}") int serverPort
    ) {
        log.info("Connecting to user profile service at {}:{}", serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext().build();

        blockingStub = UserProfileServiceGrpc.newBlockingStub(channel);
    }

    public CreateUserProfileResponse createUserProfile(CreateUserProfileRequest request) {
        CreateUserProfileResponse response = blockingStub.createUserProfile(request);
        log.info("Created user profile: {}", response);
        return response;
    }

    public CheckPhoneNumberResponse checkPhoneNumber(CheckPhoneNumberRequest request) {
        CheckPhoneNumberResponse response = blockingStub.checkPhoneNumber(request);
        log.info("Checked phone number: {}", response);
        return response;
    }

    public GetUserProfileInfoResponse getUserProfileInfo(GetUserProfileInfoRequest request) {
        GetUserProfileInfoResponse response = blockingStub.getUserProfileInfo(request);
        log.info("Get user profile info: {}", response);
        return response;
    }
}

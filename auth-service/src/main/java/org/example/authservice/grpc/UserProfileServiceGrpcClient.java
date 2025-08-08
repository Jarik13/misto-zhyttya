package org.example.authservice.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import user.profile.UserProfileServiceGrpc;

@Slf4j
@Service
public class UserProfileServiceGrpcClient {
    private final UserProfileServiceGrpc.UserProfileServiceBlockingStub blockingStub;

    public UserProfileServiceGrpcClient(
            @Value("${user-profile.service.address:localhost}") String serverAddress,
            @Value("${user-profile.service.port:4002}") int serverPort
    ) {
        log.info("Connecting to user profile service at {}:{}", serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext().build();

        blockingStub = UserProfileServiceGrpc.newBlockingStub(channel);
    }
}

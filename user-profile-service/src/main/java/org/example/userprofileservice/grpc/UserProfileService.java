package org.example.userprofileservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import user.profile.UserProfileServiceGrpc;
import user.profile.CreateUserProfileRequest;
import user.profile.CreateUserProfileResponse;
import user.profile.CheckPhoneNumberRequest;
import user.profile.CheckPhoneNumberResponse;

@Slf4j
@GrpcService
public class UserProfileService extends UserProfileServiceGrpc.UserProfileServiceImplBase {
    @Override
    public void createUserProfile(CreateUserProfileRequest request,
                                  StreamObserver<CreateUserProfileResponse> responseObserver) {
        super.createUserProfile(request, responseObserver);
    }

    @Override
    public void checkPhoneNumber(CheckPhoneNumberRequest request,
                                 StreamObserver<CheckPhoneNumberResponse> responseObserver) {
        super.checkPhoneNumber(request, responseObserver);
    }
}

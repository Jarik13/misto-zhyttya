package org.example.userprofileservice.grpc;

import io.grpc.stub.StreamObserver;
import jakarta.persistence.EntityNotFoundException;
import org.example.userprofileservice.dto.profile.ProfileRequest;
import org.example.userprofileservice.exception.ValidationException;
import org.example.userprofileservice.kafka.UserProfileProducer;
import org.example.userprofileservice.model.Gender;
import org.example.userprofileservice.model.Profile;
import org.example.userprofileservice.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.profile.*;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserProfileServiceTests {
    private UserProfileRepository repository;
    private UserProfileProducer producer;
    private UserProfileService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserProfileRepository.class);
        producer = mock(UserProfileProducer.class);
        service = new UserProfileService(repository, producer);
    }

    @Test
    void givenExistingUser_whenCreateUserProfile_thenReturnsProfile() {
        String userId = "user123";
        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setUsername("John");
        profile.setAvatarKey("avatar1");

        when(repository.findByUserId(userId)).thenReturn(Optional.of(profile));

        CreateUserProfileRequest request = CreateUserProfileRequest.newBuilder()
                .setUserId(userId)
                .build();

        StreamObserver<CreateUserProfileResponse> observer = mock(StreamObserver.class);

        service.createUserProfile(request, observer);

        verify(observer).onNext(argThat((CreateUserProfileResponse response) ->
                response.getUsername().equals("John") &&
                response.getAvatarKey().equals("avatar1")
        ));
        verify(observer).onCompleted();
    }

    @Test
    void givenPhoneNumberExists_whenCheckPhoneNumber_thenReturnsFalse() {
        String phone = "+380501234567";
        when(repository.existsByPhoneNumber(phone)).thenReturn(true);

        CheckPhoneNumberRequest request = CheckPhoneNumberRequest.newBuilder()
                .setPhoneNumber(phone)
                .build();

        StreamObserver<CheckPhoneNumberResponse> observer = mock(StreamObserver.class);

        service.checkPhoneNumber(request, observer);

        verify(observer).onNext(argThat((CheckPhoneNumberResponse response) ->
                !response.getIsUnique()
        ));
        verify(observer).onCompleted();
    }

    @Test
    void givenUserExists_whenGetUserProfileInfo_thenReturnsProfile() {
        String userId = "user123";
        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setUsername("Alice");
        profile.setAvatarKey("avatarKey");
        profile.setGender(Gender.MALE);

        when(repository.findByUserId(userId)).thenReturn(Optional.of(profile));

        GetUserProfileInfoRequest request = GetUserProfileInfoRequest.newBuilder()
                .setUserId(userId)
                .build();

        StreamObserver<GetUserProfileInfoResponse> observer = mock(StreamObserver.class);

        service.getUserProfileInfo(request, observer);

        verify(observer).onNext(argThat((GetUserProfileInfoResponse response) ->
                response.getUsername().equals("Alice") &&
                response.getAvatarKey().equals("avatarKey")
        ));
        verify(observer).onCompleted();
    }

    @Test
    void givenUserNotFound_whenFindUserProfile_thenThrowsException() {
        String userId = "unknown";
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> service.findUserProfile(userId));

        assertTrue(ex.getMessage().contains(userId));
    }

    @Test
    void givenValidUpdateRequest_whenUpdateUserProfile_thenProfileUpdatedAndAvatarEventSent() {
        String userId = "user1";
        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setUsername("Old");
        profile.setPhoneNumber("+380501234567");
        profile.setGender(Gender.MALE);
        profile.setAvatarKey("oldAvatar");

        when(repository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(repository.existsByPhoneNumberAndUserIdNot("+380501234568", userId)).thenReturn(false);

        ProfileRequest request = new ProfileRequest(
                "NewName", "+380501234568", LocalDate.now(), 1L, "newAvatar"
        );

        service.updateUserProfile(userId, request);

        assertEquals("NewName", profile.getUsername());
        assertEquals("+380501234568", profile.getPhoneNumber());
        assertEquals("newAvatar", profile.getAvatarKey());

        verify(producer).sendAvatarEvent("APPROVED", "newAvatar");
        verify(producer).sendAvatarEvent("DELETED", "oldAvatar");
        verify(repository).save(profile);
    }

    @Test
    void givenPhoneNumberTaken_whenUpdateUserProfile_thenThrowsValidationException() {
        String userId = "user1";
        Profile profile = new Profile();
        profile.setUserId(userId);

        when(repository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(repository.existsByPhoneNumberAndUserIdNot("+380501234567", userId)).thenReturn(true);

        ProfileRequest request = new ProfileRequest(
                "Name", "+380501234567", LocalDate.now(), 1L, "avatarKey"
        );

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.updateUserProfile(userId, request));

        assertNotNull(ex.getMessage());
    }
}

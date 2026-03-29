package com.solusi.erp.security.service;

import com.solusi.erp.master.party.application.dto.PartyReference;
import com.solusi.erp.master.party.application.usecase.query.GetPartyReferenceUseCase;
import com.solusi.erp.security.dto.ProfileRequest;
import com.solusi.erp.security.dto.ProfileResponse;
import com.solusi.erp.security.dto.UserRequest;
import com.solusi.erp.security.dto.UserResponse;
import com.solusi.erp.security.mapper.UserMapper;
import com.solusi.erp.security.model.Role;
import com.solusi.erp.security.model.User;
import com.solusi.erp.security.model.UserProfile;
import com.solusi.erp.security.repository.RoleRepository;
import com.solusi.erp.security.repository.UserRepository;
import com.solusi.erp.security.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private GetPartyReferenceUseCase getPartyReferenceUseCase;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MessageSource messageSource;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, roleRepository, getPartyReferenceUseCase,
                userMapper, passwordEncoder, messageSource);
    }

    // --- enrichPartyInfo via findById ---

    @Test
    @DisplayName("findById enriches party info when user has partyId")
    void findById_enrichesPartyInfo_whenUserHasPartyId() {
        User user = userWithPartyId(10L);
        PartyReference ref = new PartyReference(10L, "CUST-001", "PT Maju Jaya");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(getPartyReferenceUseCase.execute(10L)).thenReturn(Optional.of(ref));
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder()
                .partyId(10L).partyCode("CUST-001").partyName("PT Maju Jaya").build());

        UserResponse response = service.findById(1L);

        assertThat(user.getPartyCode()).isEqualTo("CUST-001");
        assertThat(user.getPartyName()).isEqualTo("PT Maju Jaya");
        verify(getPartyReferenceUseCase).execute(10L);
    }

    @Test
    @DisplayName("findById skips party lookup when user has no partyId")
    void findById_skipsPartyLookup_whenNoPartyId() {
        User user = userWithPartyId(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().build());

        service.findById(1L);

        assertThat(user.getPartyCode()).isNull();
        assertThat(user.getPartyName()).isNull();
        verifyNoInteractions(getPartyReferenceUseCase);
    }

    @Test
    @DisplayName("findById throws when linked party not found in party module")
    void findById_throws_whenLinkedPartyNotFound() {
        User user = userWithPartyId(99L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(getPartyReferenceUseCase.execute(99L)).thenReturn(Optional.empty());
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Party not found");

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("findById throws when user not found")
    void findById_throws_whenUserNotFound() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("User not found");

        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(RuntimeException.class);
    }

    // --- applyPartyReference via create ---

    @Test
    @DisplayName("create sets party reference when partyId is provided")
    void create_setsPartyReference_whenPartyIdProvided() {
        UserRequest request = UserRequest.builder()
                .username("john").email("john@test.com").password("secret1")
                .roleId(1L).fullName("John Doe").partyId(10L).build();
        Role role = new Role();
        User userEntity = new User();
        UserProfile profile = new UserProfile();
        PartyReference ref = new PartyReference(10L, "CUST-001", "PT Maju Jaya");

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(userMapper.toProfileEntity(request)).thenReturn(profile);
        when(passwordEncoder.encode("secret1")).thenReturn("hashed");
        when(getPartyReferenceUseCase.execute(10L)).thenReturn(Optional.of(ref));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(UserResponse.builder().build());

        service.create(request);

        assertThat(userEntity.getPartyId()).isEqualTo(10L);
        assertThat(userEntity.getPartyCode()).isEqualTo("CUST-001");
        assertThat(userEntity.getPartyName()).isEqualTo("PT Maju Jaya");
        verify(getPartyReferenceUseCase).execute(10L);
    }

    @Test
    @DisplayName("create skips party assignment when partyId is null")
    void create_skipsPartyAssignment_whenPartyIdNull() {
        UserRequest request = UserRequest.builder()
                .username("jane").email("jane@test.com").password("pass123")
                .roleId(1L).fullName("Jane Doe").partyId(null).build();
        Role role = new Role();
        User userEntity = new User();
        UserProfile profile = new UserProfile();

        when(userRepository.findByUsername("jane")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(userMapper.toProfileEntity(request)).thenReturn(profile);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.toResponse(userEntity)).thenReturn(UserResponse.builder().build());

        service.create(request);

        assertThat(userEntity.getPartyId()).isNull();
        verifyNoInteractions(getPartyReferenceUseCase);
    }

    @Test
    @DisplayName("create throws when password is too short")
    void create_throws_whenPasswordTooShort() {
        UserRequest request = UserRequest.builder()
                .username("short").email("short@test.com").password("abc")
                .roleId(1L).fullName("Short Pass").build();
        Role role = new Role();
        when(userRepository.findByUsername("short")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("short@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Password too short");

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(RuntimeException.class);
    }

    // --- Party immutability via update ---

    @Test
    @DisplayName("update throws when trying to change partyId of an already-linked user")
    void update_throws_whenPartyIdChangedOnAlreadyLinkedUser() {
        User user = userWithPartyId(10L);
        user.setProfile(new UserProfile());
        UserRequest request = UserRequest.builder()
                .username("alice").email("alice@test.com")
                .roleId(1L).fullName("Alice").partyId(99L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(new Role()));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Party immutable");

        assertThatThrownBy(() -> service.update(1L, request))
                .isInstanceOf(RuntimeException.class);
        verify(getPartyReferenceUseCase, never()).execute(anyLong());
    }

    @Test
    @DisplayName("update allows setting partyId for the first time")
    void update_allowsSettingPartyId_whenUserHasNoParty() {
        User user = userWithPartyId(null);
        UserProfile profile = new UserProfile();
        user.setProfile(profile);
        PartyReference ref = new PartyReference(5L, "VEND-001", "PT Supplier");
        UserRequest request = UserRequest.builder()
                .username("bob").email("bob@test.com")
                .roleId(1L).fullName("Bob").partyId(5L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findById(1L)).thenReturn(Optional.of(new Role()));
        when(getPartyReferenceUseCase.execute(5L)).thenReturn(Optional.of(ref));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().build());

        service.update(1L, request);

        assertThat(user.getPartyId()).isEqualTo(5L);
        // called twice: once in applyPartyReference, once in enrichPartyInfo on return
        verify(getPartyReferenceUseCase, times(2)).execute(5L);
    }

    // --- Profile ---

    @Test
    @DisplayName("getProfile enriches party info from party module")
    void getProfile_enrichesPartyInfo() {
        User user = userWithPartyId(7L);
        PartyReference ref = new PartyReference(7L, "EMP-001", "John Employee");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(getPartyReferenceUseCase.execute(7L)).thenReturn(Optional.of(ref));
        when(userMapper.toProfileResponse(user)).thenReturn(ProfileResponse.builder().build());

        service.getProfile("john");

        assertThat(user.getPartyCode()).isEqualTo("EMP-001");
        assertThat(user.getPartyName()).isEqualTo("John Employee");
    }

    @Test
    @DisplayName("updateProfile changes password when current password matches")
    void updateProfile_changesPassword_whenCurrentPasswordMatches() {
        User user = userWithPartyId(null);
        user.setPassword("hashed-current");
        user.setProfile(new UserProfile());
        user.setEmail("old@test.com");

        ProfileRequest request = new ProfileRequest();
        request.setEmail("old@test.com");
        request.setCurrentPassword("current123");
        request.setNewPassword("newpass1");
        request.setConfirmPassword("newpass1");

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("old@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.matches("current123", "hashed-current")).thenReturn(true);
        when(passwordEncoder.encode("newpass1")).thenReturn("hashed-new");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toProfileResponse(user)).thenReturn(ProfileResponse.builder().build());

        service.updateProfile("user1", request);

        assertThat(user.getPassword()).isEqualTo("hashed-new");
        assertThat(user.isPasswordChangeRequired()).isFalse();
    }

    @Test
    @DisplayName("updateProfile throws when current password is wrong")
    void updateProfile_throws_whenCurrentPasswordWrong() {
        User user = userWithPartyId(null);
        user.setPassword("hashed-current");
        user.setProfile(new UserProfile());
        user.setEmail("u@test.com");

        ProfileRequest request = new ProfileRequest();
        request.setEmail("u@test.com");
        request.setCurrentPassword("wrongpass");
        request.setNewPassword("newpass1");
        request.setConfirmPassword("newpass1");

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.matches("wrongpass", "hashed-current")).thenReturn(false);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Password mismatch");

        assertThatThrownBy(() -> service.updateProfile("user1", request))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("updateProfile throws when confirm password does not match new password")
    void updateProfile_throws_whenConfirmPasswordMismatch() {
        User user = userWithPartyId(null);
        user.setPassword("hashed");
        user.setProfile(new UserProfile());
        user.setEmail("u@test.com");

        ProfileRequest request = new ProfileRequest();
        request.setEmail("u@test.com");
        request.setCurrentPassword("current");
        request.setNewPassword("newpass1");
        request.setConfirmPassword("different");

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.matches("current", "hashed")).thenReturn(true);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Confirm mismatch");

        assertThatThrownBy(() -> service.updateProfile("user1", request))
                .isInstanceOf(RuntimeException.class);
    }

    // --- Helpers ---

    private User userWithPartyId(Long partyId) {
        User u = new User();
        u.setPartyId(partyId);
        return u;
    }
}

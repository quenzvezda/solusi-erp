package com.solusi.erp.security.user.infrastructure.config;

import com.solusi.erp.master.party.application.usecase.query.GetPartyReferenceUseCase;
import com.solusi.erp.security.permissiongroup.application.usecase.query.FindPermissionGroupsUseCase;
import com.solusi.erp.security.permissiongroup.web.mapper.PermissionGroupWebMapper;
import com.solusi.erp.security.user.application.port.PartyReferenceGateway;
import com.solusi.erp.security.user.application.port.PasswordCipher;
import com.solusi.erp.security.user.application.usecase.command.*;
import com.solusi.erp.security.user.application.usecase.query.*;
import com.solusi.erp.security.user.domain.repository.UserRepository;
import com.solusi.erp.security.user.infrastructure.adapter.PartyReferenceGatewayAdapter;
import com.solusi.erp.security.user.infrastructure.adapter.PasswordCipherAdapter;
import com.solusi.erp.security.role.infrastructure.persistence.RoleJpaRepository;
import com.solusi.erp.security.user.infrastructure.adapter.UserRepositoryAdapter;
import com.solusi.erp.security.user.infrastructure.persistence.UserJpaRepository;
import com.solusi.erp.security.user.infrastructure.persistence.UserPersistenceMapper;
import com.solusi.erp.security.user.service.UserService;
import com.solusi.erp.security.user.service.impl.UserServiceImpl;
import com.solusi.erp.security.user.web.mapper.UserWebMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class UserConfig {

    @Bean
    public UserRepository userDomainRepository(UserJpaRepository userJpaRepository,
                                               RoleJpaRepository roleRepository,
                                               UserPersistenceMapper mapper) {
        return new UserRepositoryAdapter(userJpaRepository, roleRepository, mapper);
    }

    @Bean
    public PasswordCipher userPasswordCipher(PasswordEncoder passwordEncoder) {
        return new PasswordCipherAdapter(passwordEncoder);
    }

    @Bean
    public PartyReferenceGateway partyReferenceGateway(GetPartyReferenceUseCase getPartyReferenceUseCase) {
        return new PartyReferenceGatewayAdapter(getPartyReferenceUseCase);
    }

    @Bean
    public CreateUserUseCase createUserUseCase(UserRepository repository,
                                                PasswordCipher cipher,
                                                PartyReferenceGateway partyReferenceGateway,
                                                PlatformTransactionManager txManager) {
        CreateUserUseCase pure = new CreateUserUseCaseImpl(repository, cipher, partyReferenceGateway);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (username, email, rawPassword, roleId, fullName, phoneNumber, enabled, partyId) ->
                tx.execute(status -> pure.execute(username, email, rawPassword, roleId, fullName, phoneNumber, enabled, partyId));
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase(UserRepository repository,
                                                PasswordCipher cipher,
                                                PartyReferenceGateway partyReferenceGateway,
                                                PlatformTransactionManager txManager) {
        UpdateUserUseCase pure = new UpdateUserUseCaseImpl(repository, cipher, partyReferenceGateway);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (id, username, email, rawPassword, roleId, fullName, phoneNumber, enabled, passwordChangeRequired, partyId) ->
                tx.execute(status -> pure.execute(id, username, email, rawPassword, roleId, fullName, phoneNumber, enabled, passwordChangeRequired, partyId));
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepository repository,
                                                PlatformTransactionManager txManager) {
        DeleteUserUseCase pure = new DeleteUserUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.executeWithoutResult(status -> pure.execute(id));
    }

    @Bean
    public ToggleUserStatusUseCase toggleUserStatusUseCase(UserRepository repository,
                                                            PlatformTransactionManager txManager) {
        ToggleUserStatusUseCase pure = new ToggleUserStatusUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return id -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public UpdateProfileUseCase updateProfileUseCase(UserRepository repository,
                                                      PasswordCipher cipher,
                                                      PlatformTransactionManager txManager) {
        UpdateProfileUseCase pure = new UpdateProfileUseCaseImpl(repository, cipher);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        return (username, fullName, email, phoneNumber, languageCode, defaultPageSize, theme, currentPassword, newPassword, confirmPassword) ->
                tx.execute(status -> pure.execute(username, fullName, email, phoneNumber, languageCode, defaultPageSize, theme, currentPassword, newPassword, confirmPassword));
    }

    @Bean
    public FindUsersUseCase findUsersUseCase(UserRepository repository,
                                              PlatformTransactionManager txManager) {
        FindUsersUseCase pure = new FindUsersUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return (keyword, pageable) -> tx.execute(status -> pure.execute(keyword, pageable));
    }

    @Bean
    public FindUserByIdUseCase findUserByIdUseCase(UserRepository repository,
                                                    PlatformTransactionManager txManager) {
        FindUserByIdUseCase pure = new FindUserByIdUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return id -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetUserEditViewUseCase getUserEditViewUseCase(UserRepository repository,
                                                          PlatformTransactionManager txManager) {
        GetUserEditViewUseCase pure = new GetUserEditViewUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return id -> tx.execute(status -> pure.execute(id));
    }

    @Bean
    public GetProfileUseCase getProfileUseCase(UserRepository repository,
                                                PlatformTransactionManager txManager) {
        GetProfileUseCase pure = new GetProfileUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return username -> tx.execute(status -> pure.execute(username));
    }

    @Bean
    public GetProfileUpdateDataUseCase getProfileUpdateDataUseCase(UserRepository repository,
                                                                    PlatformTransactionManager txManager) {
        GetProfileUpdateDataUseCase pure = new GetProfileUpdateDataUseCaseImpl(repository);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setReadOnly(true);
        return username -> tx.execute(status -> pure.execute(username));
    }

    @Bean
    public UserService userService(CreateUserUseCase createUserUseCase,
                                   UpdateUserUseCase updateUserUseCase,
                                   DeleteUserUseCase deleteUserUseCase,
                                   ToggleUserStatusUseCase toggleUserStatusUseCase,
                                   UpdateProfileUseCase updateProfileUseCase,
                                   FindUsersUseCase findUsersUseCase,
                                   FindUserByIdUseCase findUserByIdUseCase,
                                   GetUserEditViewUseCase getUserEditViewUseCase,
                                   GetProfileUseCase getProfileUseCase,
                                   GetProfileUpdateDataUseCase getProfileUpdateDataUseCase,
                                   com.solusi.erp.security.role.application.usecase.query.FindRolesUseCase findRolesUseCase,
                                   FindPermissionGroupsUseCase findPermissionGroupsUseCase,
                                   PartyReferenceGateway partyReferenceGateway,
                                   UserWebMapper userWebMapper,
                                   PermissionGroupWebMapper permissionGroupWebMapper,
                                   MessageSource messageSource) {
        return new UserServiceImpl(createUserUseCase, updateUserUseCase, deleteUserUseCase,
                toggleUserStatusUseCase, updateProfileUseCase, findUsersUseCase, findUserByIdUseCase,
                getUserEditViewUseCase, getProfileUseCase, getProfileUpdateDataUseCase,
                findRolesUseCase, findPermissionGroupsUseCase, partyReferenceGateway,
                userWebMapper, permissionGroupWebMapper, messageSource);
    }
}

package com.spectrayan.synaptiq.auth.application.service;

import com.spectrayan.synaptiq.auth.application.port.in.UserProfileUseCase;
import com.spectrayan.synaptiq.auth.application.port.out.UserPersistencePort;
import com.spectrayan.synaptiq.auth.domain.model.User;
import com.spectrayan.synaptiq.shared.exception.ErrorCode;
import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import com.spectrayan.synaptiq.shared.exception.SynaptiqException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService implements UserProfileUseCase {
    private final UserPersistencePort userPersistence;
    private final BCryptPasswordEncoder encoder;

    @Override
    public Mono<User> getCurrentUser(String uid) {
        return userPersistence.findById(uid)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")));
    }

    @Override
    public Mono<User> updateRole(String userId, String newRole) {
        return userPersistence.findById(userId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
            .flatMap(user -> {
                user.setRole(newRole);
                log.info("Role updated for user={} to role={}", userId, newRole);
                return userPersistence.save(user);
            });
    }

    @Override
    public Mono<Void> changePassword(String userId, String currentPassword, String newPassword) {
        return userPersistence.findById(userId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
            .flatMap(user -> {
                if (!encoder.matches(currentPassword, user.getPasswordHash())) {
                    return Mono.error(new SynaptiqException(
                        ErrorCode.AUTHENTICATION_FAILED, "Current password is incorrect"));
                }
                user.setPasswordHash(encoder.encode(newPassword));
                log.info("Password changed for user={}", userId);
                return userPersistence.save(user).then();
            });
    }
}

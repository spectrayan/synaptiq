package com.spectrayan.synaptiq.auth.application.port.in;

import com.spectrayan.synaptiq.auth.domain.model.User;
import reactor.core.publisher.Mono;

/**
 * Inbound port for user profile operations: getCurrentUser, updateRole, changePassword.
 */
public interface UserProfileUseCase {

    /** Get the current user's profile by ID (extracted from JWT). */
    Mono<User> getCurrentUser(String userId);

    /** Update a user's role (admin only). */
    Mono<User> updateRole(String userId, String newRole);

    /** Change the current user's password. */
    Mono<Void> changePassword(String userId, String currentPassword, String newPassword);
}

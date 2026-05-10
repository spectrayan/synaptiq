package com.spectrayan.synaptiq.auth.infrastructure.web;

import com.spectrayan.synaptiq.auth.application.port.in.LoginUseCase;
import com.spectrayan.synaptiq.auth.application.port.in.RefreshTokenUseCase;
import com.spectrayan.synaptiq.auth.application.port.in.SignupUseCase;
import com.spectrayan.synaptiq.auth.application.port.in.UserProfileUseCase;
import com.spectrayan.synaptiq.auth.domain.model.User;
import com.spectrayan.synaptiq.infrastructure.in.web.api.AuthApi;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Auth controller — implements the generated {@link AuthApi} interface.
 * All annotations come from the OpenAPI spec.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {
    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final UserProfileUseCase userProfileUseCase;

    @Override
    public Mono<ResponseEntity<UserResponse>> signup(Mono<SignupRequest> req, ServerWebExchange exchange) {
        return req.flatMap(r -> signupUseCase.signup(
                new SignupUseCase.SignupCommand(r.getEmail(), r.getPassword()))
            .map(u -> ResponseEntity.status(201).body(toUserDto(u))));
    }

    @Override
    public Mono<ResponseEntity<AuthTokenResponse>> login(Mono<LoginRequest> req, ServerWebExchange exchange) {
        return req
            .flatMap(r -> loginUseCase.login(
                new LoginUseCase.LoginCommand(r.getEmail(), r.getPassword()))
            .map(t -> ResponseEntity.ok(toTokenDto(t))));
    }

    @Override
    public Mono<ResponseEntity<AuthTokenResponse>> refreshToken(ServerWebExchange exchange) {
        return resolveUserId(exchange)
            .flatMap(userId -> {
                // Extract refresh token from Authorization header (Bearer <token>)
                String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
                String bearerToken = authHeader != null && authHeader.startsWith("Bearer ")
                    ? authHeader.substring(7)
                    : "";
                return refreshTokenUseCase.refresh(bearerToken, userId);
            })
            .map(t -> ResponseEntity.ok(toTokenDto(t)));
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> getCurrentUser(ServerWebExchange exchange) {
        return resolveUserId(exchange)
            .flatMap(userProfileUseCase::getCurrentUser)
            .map(u -> ResponseEntity.ok(toUserDto(u)));
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> updateUserRole(Mono<UpdateRoleRequest> req, ServerWebExchange exchange) {
        return resolveUserId(exchange)
            .flatMap(userId -> req.flatMap(r ->
                userProfileUseCase.updateRole(userId, r.getRole())))
            .map(u -> ResponseEntity.ok(toUserDto(u)));
    }

    @Override
    public Mono<ResponseEntity<Void>> changePassword(Mono<ChangePasswordRequest> req, ServerWebExchange exchange) {
        return resolveUserId(exchange)
            .flatMap(userId -> req.flatMap(r ->
                userProfileUseCase.changePassword(
                    userId, r.getCurrentPassword(), r.getNewPassword())))
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    // ── Helpers ──

    private Mono<String> resolveUserId(ServerWebExchange exchange) {
        return exchange.getPrincipal()
            .map(p -> p.getName())
            .switchIfEmpty(Mono.error(new com.spectrayan.synaptiq.shared.exception.SynaptiqException(
                com.spectrayan.synaptiq.shared.exception.ErrorCode.AUTHENTICATION_FAILED,
                "No authenticated user")));
    }

    private UserResponse toUserDto(User u) {
        return new UserResponse()
            .uid(u.getId()).email(u.getEmail())
            .displayName(u.getDisplayName() != null ? u.getDisplayName() : "")
            .role(u.getRole())
            .emailVerified(u.isEmailVerified())
            .tenantId(u.getTenantId());
    }

    private AuthTokenResponse toTokenDto(LoginUseCase.AuthToken t) {
        return new AuthTokenResponse()
            .idToken(t.idToken())
            .refreshToken(t.refreshToken())
            .expiresIn(t.expiresIn());
    }
}

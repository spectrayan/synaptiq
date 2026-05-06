package com.synaptiq.auth.infrastructure.web;

import com.synaptiq.auth.application.port.in.LoginUseCase;
import com.synaptiq.auth.application.port.in.SignupUseCase;
import com.synaptiq.auth.domain.model.User;
import com.synaptiq.infrastructure.in.web.api.AuthApi;
import com.synaptiq.infrastructure.in.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {
    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;

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
            .map(t -> ResponseEntity.ok(new AuthTokenResponse()
                .idToken(t.idToken())
                .refreshToken(t.refreshToken())
                .expiresIn(t.expiresIn()))));
    }

    @Override
    public Mono<ResponseEntity<AuthTokenResponse>> refreshToken(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(new AuthTokenResponse()));
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> getCurrentUser(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(new UserResponse()));
    }

    @Override
    public Mono<ResponseEntity<UserResponse>> updateUserRole(Mono<UpdateRoleRequest> req, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(new UserResponse()));
    }

    @Override
    public Mono<ResponseEntity<Void>> changePassword(Mono<ChangePasswordRequest> req, ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.noContent().build());
    }

    private UserResponse toUserDto(User u) {
        return new UserResponse()
            .uid(u.getId()).email(u.getEmail())
            .displayName(u.getDisplayName() != null ? u.getDisplayName() : "")
            .role(u.getRole())
            .emailVerified(u.isEmailVerified())
            .tenantId(u.getTenantId());
    }
}

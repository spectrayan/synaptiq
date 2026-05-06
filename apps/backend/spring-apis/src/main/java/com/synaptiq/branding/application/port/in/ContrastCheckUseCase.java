package com.synaptiq.branding.application.port.in;

import reactor.core.publisher.Mono;

public interface ContrastCheckUseCase {
    Mono<ContrastResult> checkContrast(String fg, String bg);

    record ContrastResult(
        String foreground,
        String background,
        double ratio,
        boolean aaNormal,
        boolean aaLarge,
        boolean aaaNormal
    ) {}
}

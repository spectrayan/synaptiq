package com.spectrayan.synaptiq.branding.application.service;

import com.spectrayan.synaptiq.branding.application.port.in.ContrastCheckUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ContrastCheckService implements ContrastCheckUseCase {

    @Override
    public Mono<ContrastResult> checkContrast(String fg, String bg) {
        double ratio = calculateContrastRatio(fg, bg);
        return Mono.just(new ContrastResult(fg, bg, ratio, ratio >= 4.5, ratio >= 3.0, ratio >= 7.0));
    }

    private double calculateContrastRatio(String fg, String bg) {
        double fgL = relativeLuminance(fg);
        double bgL = relativeLuminance(bg);
        double lighter = Math.max(fgL, bgL);
        double darker = Math.min(fgL, bgL);
        return (lighter + 0.05) / (darker + 0.05);
    }

    private double relativeLuminance(String hex) {
        if (hex == null || hex.length() < 7) return 0;
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        double rN = r / 255.0, gN = g / 255.0, bN = b / 255.0;
        rN = rN <= 0.03928 ? rN / 12.92 : Math.pow((rN + 0.055) / 1.055, 2.4);
        gN = gN <= 0.03928 ? gN / 12.92 : Math.pow((gN + 0.055) / 1.055, 2.4);
        bN = bN <= 0.03928 ? bN / 12.92 : Math.pow((bN + 0.055) / 1.055, 2.4);
        return 0.2126 * rN + 0.7152 * gN + 0.0722 * bN;
    }
}

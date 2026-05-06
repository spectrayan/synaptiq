package com.synaptiq;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Validates Spring Modulith module structure and dependencies.
 */
class ModularityTests {

    @Test
    void verifyModularity() {
        ApplicationModules.of(SynaptiqApplication.class).verify();
    }
}

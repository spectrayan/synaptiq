/**
 * Application module — manages tenant applications, AI personas, and component configuration.
 * Open module: exposes domain model and ports to dependent modules.
 */
@org.springframework.modulith.ApplicationModule(
    type = org.springframework.modulith.ApplicationModule.Type.OPEN,
    allowedDependencies = {"shared", "infrastructure"}
)
package com.spectrayan.synaptiq.application;

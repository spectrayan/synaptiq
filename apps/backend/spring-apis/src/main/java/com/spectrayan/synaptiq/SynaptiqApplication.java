package com.spectrayan.synaptiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Synaptiq Spring Boot Application — modular monolith entry point.
 *
 * <p>Modules:
 * <ul>
 *   <li>{@code shared} — cross-cutting config, security, exceptions, properties</li>
 *   <li>{@code tenant} — tenant provisioning, CRUD, admin management</li>
 *   <li>{@code datasource} — multi-source data connections, schema management</li>
 *   <li>{@code integration} — Camel-based dynamic connectors, templates, route lifecycle</li>
 *   <li>{@code chat} — SSE streaming LLM chat, session management</li>
 *   <li>{@code auth} — signup, login, JWT (builtin + Firebase)</li>
 *   <li>{@code workflow} — generate, execute, CRUD, tools, WebSocket sync</li>
 *   <li>{@code analytics} — summary, tokens, billing, platform rollup</li>
 *   <li>{@code action} — action dispatch, saved items, audit log</li>
 *   <li>{@code branding} — branding, themes, logo, contrast check</li>
 *   <li>{@code tenantconfig} — AI persona, guardrails, LLM provider, components</li>
 *   <li>{@code notification} — in-app notifications, event-driven fan-out, SSE push</li>
 *   <li>{@code schemaregistry} — generic data introspection</li>
 * </ul>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@Modulithic(
    systemName = "Synaptiq",
    sharedModules = {"shared"}
)
public class SynaptiqApplication {

    public static void main(String[] args) {
        SpringApplication.run(SynaptiqApplication.class, args);
    }
}


package com.synaptiq.tenant.infrastructure.persistence.mongo.mapper;

import com.synaptiq.tenant.domain.model.*;
import com.synaptiq.tenant.infrastructure.persistence.mongo.entity.TenantRegistryDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper between Tenant domain model and TenantRegistryDocument.
 * Maps org-level fields only. Application-specific config lives in the Application module.
 */
@Mapper(componentModel = "spring", imports = {TenantStatus.class, AccessMode.class, PlanTier.class})
public interface TenantPersistenceMapper {

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", expression = "java(tenant.getStatus() != null ? tenant.getStatus().name() : \"ONBOARDING\")")
    @Mapping(target = "accessMode", expression = "java(tenant.getAccessMode() != null ? tenant.getAccessMode().name() : \"PUBLIC\")")
    @Mapping(target = "planTier", expression = "java(tenant.getPlanTier() != null ? tenant.getPlanTier().name() : \"FREE\")")
    TenantRegistryDocument toDocument(Tenant tenant);

    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "status", expression = "java(safeEnum(doc.getStatus(), TenantStatus.class, TenantStatus.ONBOARDING))")
    @Mapping(target = "accessMode", expression = "java(safeEnum(doc.getAccessMode(), AccessMode.class, AccessMode.PUBLIC))")
    @Mapping(target = "planTier", expression = "java(safeEnum(doc.getPlanTier(), PlanTier.class, PlanTier.FREE))")
    Tenant toDomain(TenantRegistryDocument doc);

    // ── Sub-document mappers (auto-discovered by MapStruct) ─────────

    TenantRegistryDocument.TenantLimitsEmbed toEmbed(Tenant.TenantLimits limits);
    Tenant.TenantLimits toDomain(TenantRegistryDocument.TenantLimitsEmbed embed);

    TenantRegistryDocument.LlmProviderEmbed toEmbed(Tenant.LlmProvider provider);
    Tenant.LlmProvider toDomain(TenantRegistryDocument.LlmProviderEmbed embed);

    TenantRegistryDocument.TenantAdminEmbed toEmbed(Tenant.TenantAdmin admin);
    Tenant.TenantAdmin toDomain(TenantRegistryDocument.TenantAdminEmbed embed);

    default <E extends Enum<E>> E safeEnum(String value, Class<E> enumClass, E defaultValue) {
        if (value == null) return defaultValue;
        try { return Enum.valueOf(enumClass, value); }
        catch (IllegalArgumentException e) { return defaultValue; }
    }
}

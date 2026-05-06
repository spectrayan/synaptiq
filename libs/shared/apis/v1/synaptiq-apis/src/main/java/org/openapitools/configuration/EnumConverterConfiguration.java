package org.openapitools.configuration;

import com.synaptiq.infrastructure.in.web.dto.AccessMode;
import com.synaptiq.infrastructure.in.web.dto.ActionOutcome;
import com.synaptiq.infrastructure.in.web.dto.AdminRole;
import com.synaptiq.infrastructure.in.web.dto.ApplicationStatus;
import com.synaptiq.infrastructure.in.web.dto.CatalogItemStatus;
import com.synaptiq.infrastructure.in.web.dto.ConversationRole;
import com.synaptiq.infrastructure.in.web.dto.CredentialType;
import com.synaptiq.infrastructure.in.web.dto.DataSourceStatus;
import com.synaptiq.infrastructure.in.web.dto.DataSourceType;
import com.synaptiq.infrastructure.in.web.dto.NodeExecutionStatus;
import com.synaptiq.infrastructure.in.web.dto.PlanTier;
import com.synaptiq.infrastructure.in.web.dto.TenantStatus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

/**
 * This class provides Spring Converter beans for the enum models in the OpenAPI specification.
 *
 * By default, Spring only converts primitive types to enums using Enum::valueOf, which can prevent
 * correct conversion if the OpenAPI specification is using an `enumPropertyNaming` other than
 * `original` or the specification has an integer enum.
 */
@Configuration(value = "org.openapitools.configuration.enumConverterConfiguration")
public class EnumConverterConfiguration {

    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.accessModeConverter")
    Converter<String, AccessMode> accessModeConverter() {
        return new Converter<String, AccessMode>() {
            @Override
            public AccessMode convert(String source) {
                return AccessMode.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.actionOutcomeConverter")
    Converter<String, ActionOutcome> actionOutcomeConverter() {
        return new Converter<String, ActionOutcome>() {
            @Override
            public ActionOutcome convert(String source) {
                return ActionOutcome.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.adminRoleConverter")
    Converter<String, AdminRole> adminRoleConverter() {
        return new Converter<String, AdminRole>() {
            @Override
            public AdminRole convert(String source) {
                return AdminRole.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.applicationStatusConverter")
    Converter<String, ApplicationStatus> applicationStatusConverter() {
        return new Converter<String, ApplicationStatus>() {
            @Override
            public ApplicationStatus convert(String source) {
                return ApplicationStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.catalogItemStatusConverter")
    Converter<String, CatalogItemStatus> catalogItemStatusConverter() {
        return new Converter<String, CatalogItemStatus>() {
            @Override
            public CatalogItemStatus convert(String source) {
                return CatalogItemStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.conversationRoleConverter")
    Converter<String, ConversationRole> conversationRoleConverter() {
        return new Converter<String, ConversationRole>() {
            @Override
            public ConversationRole convert(String source) {
                return ConversationRole.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.credentialTypeConverter")
    Converter<String, CredentialType> credentialTypeConverter() {
        return new Converter<String, CredentialType>() {
            @Override
            public CredentialType convert(String source) {
                return CredentialType.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.dataSourceStatusConverter")
    Converter<String, DataSourceStatus> dataSourceStatusConverter() {
        return new Converter<String, DataSourceStatus>() {
            @Override
            public DataSourceStatus convert(String source) {
                return DataSourceStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.dataSourceTypeConverter")
    Converter<String, DataSourceType> dataSourceTypeConverter() {
        return new Converter<String, DataSourceType>() {
            @Override
            public DataSourceType convert(String source) {
                return DataSourceType.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.nodeExecutionStatusConverter")
    Converter<String, NodeExecutionStatus> nodeExecutionStatusConverter() {
        return new Converter<String, NodeExecutionStatus>() {
            @Override
            public NodeExecutionStatus convert(String source) {
                return NodeExecutionStatus.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.planTierConverter")
    Converter<String, PlanTier> planTierConverter() {
        return new Converter<String, PlanTier>() {
            @Override
            public PlanTier convert(String source) {
                return PlanTier.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.tenantStatusConverter")
    Converter<String, TenantStatus> tenantStatusConverter() {
        return new Converter<String, TenantStatus>() {
            @Override
            public TenantStatus convert(String source) {
                return TenantStatus.fromValue(source);
            }
        };
    }

}

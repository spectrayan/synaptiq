package com.spectrayan.synaptiq.integration.template;

import com.spectrayan.synaptiq.integration.model.TemplateDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BuiltInTemplates} — validates the catalog is complete
 * and each template has required fields populated.
 */
@DisplayName("BuiltInTemplates")
class BuiltInTemplatesTest {

    @Test
    @DisplayName("should define exactly 5 built-in templates")
    void hasFiveTemplates() {
        assertThat(BuiltInTemplates.all()).hasSize(5);
    }

    @Test
    @DisplayName("each template should have required metadata")
    void eachHasRequiredMetadata() {
        for (TemplateDescriptor t : BuiltInTemplates.all()) {
            assertThat(t.getTemplateId()).as("templateId for " + t.getDisplayName()).isNotBlank();
            assertThat(t.getDisplayName()).as("displayName").isNotBlank();
            assertThat(t.getDescription()).as("description").isNotBlank();
            assertThat(t.getIcon()).as("icon").isNotBlank();
            assertThat(t.getCategory()).as("category").isNotBlank();
            assertThat(t.getConnectorType()).as("connectorType").isNotBlank();
            assertThat(t.isBuiltIn()).as("builtIn").isTrue();
            assertThat(t.getParameters()).as("parameters").isNotEmpty();
        }
    }

    @Test
    @DisplayName("REST_API_POLL should have url, method, authHeader, pollIntervalMs parameters")
    void restApiPollParameters() {
        List<String> paramNames = BuiltInTemplates.REST_API_POLL.getParameters().stream()
                .map(TemplateDescriptor.ParameterDefinition::getName)
                .toList();

        assertThat(paramNames).containsExactlyInAnyOrder("url", "method", "authHeader", "pollIntervalMs");
        assertThat(BuiltInTemplates.REST_API_POLL.getConnectorType()).isEqualTo("REST_API");
    }

    @Test
    @DisplayName("WEBHOOK_RECEIVER should require webhookPath")
    void webhookReceiverParameters() {
        var required = BuiltInTemplates.WEBHOOK_RECEIVER.getParameters().stream()
                .filter(TemplateDescriptor.ParameterDefinition::isRequired)
                .map(TemplateDescriptor.ParameterDefinition::getName)
                .toList();

        assertThat(required).contains("webhookPath");
    }

    @Test
    @DisplayName("SLACK_NOTIFY should require credentials")
    void slackNotifyRequiresCredentials() {
        assertThat(BuiltInTemplates.SLACK_NOTIFY.isRequiresCredential()).isTrue();
    }

    @Test
    @DisplayName("DATABASE_QUERY should require credentials")
    void databaseQueryRequiresCredentials() {
        assertThat(BuiltInTemplates.DATABASE_QUERY.isRequiresCredential()).isTrue();
    }

    @Test
    @DisplayName("all template IDs should be unique")
    void uniqueTemplateIds() {
        List<String> ids = BuiltInTemplates.all().stream()
                .map(TemplateDescriptor::getTemplateId)
                .toList();

        assertThat(ids).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("all connector types should be unique across built-ins")
    void uniqueConnectorTypes() {
        List<String> types = BuiltInTemplates.all().stream()
                .map(TemplateDescriptor::getConnectorType)
                .toList();

        assertThat(types).doesNotHaveDuplicates();
    }
}

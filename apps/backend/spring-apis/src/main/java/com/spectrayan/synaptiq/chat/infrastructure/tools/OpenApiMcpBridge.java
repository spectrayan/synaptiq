package com.spectrayan.synaptiq.chat.infrastructure.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Dynamic OpenAPI to MCP Tool bridge.
 * <p>
 * Parses the bundled OpenAPI specification at startup and automatically
 * exposes all documented REST API endpoints as Spring AI ToolCallbacks.
 * These tools are then automatically served over the SSE MCP server.
 */
@Slf4j
@Component
public class OpenApiMcpBridge implements ToolCallbackProvider {

    private final Resource openApiSpec;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final List<ToolCallback> toolCallbacks = new ArrayList<>();

    // Keep track of tools we've already registered (e.g. from @Tool beans) to avoid duplicates
    private final Set<String> ignoredOperations = Set.of(
        "listRoles", "getRole", "createRole", "updateRole", "deleteRole", "listScopes"
    );

    public OpenApiMcpBridge(
            @Value("classpath:openapi/bundled-synaptiq-v1.yaml") Resource openApiSpec,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper) {
        this.openApiSpec = openApiSpec;
        // The MCP tools will proxy calls back to our own server.
        // In a real multi-pod setup, this could route through the internal gateway.
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
        this.objectMapper = objectMapper;
        
        initializeTools();
    }

    private void initializeTools() {
        try {
            log.info("Loading OpenAPI spec for MCP bridge from {}", openApiSpec.getURI());
            OpenAPI spec = new OpenAPIV3Parser().read(openApiSpec.getURI().toString());
            
            if (spec == null || spec.getPaths() == null) {
                log.warn("Failed to parse OpenAPI spec or spec is empty");
                return;
            }

            // Group operations by tag
            Map<String, List<OperationDetails>> operationsByTag = new HashMap<>();

            spec.getPaths().forEach((path, pathItem) -> {
                extractOperations(path, HttpMethod.GET, pathItem.getGet(), operationsByTag);
                extractOperations(path, HttpMethod.POST, pathItem.getPost(), operationsByTag);
                extractOperations(path, HttpMethod.PUT, pathItem.getPut(), operationsByTag);
                extractOperations(path, HttpMethod.DELETE, pathItem.getDelete(), operationsByTag);
                extractOperations(path, HttpMethod.PATCH, pathItem.getPatch(), operationsByTag);
            });
            
            // Create one composite tool per tag
            for (Map.Entry<String, List<OperationDetails>> entry : operationsByTag.entrySet()) {
                createCompositeToolForTag(entry.getKey(), entry.getValue());
            }
            
            log.info("Registered {} composite MCP tools from OpenAPI spec tags", toolCallbacks.size());
            
        } catch (IOException e) {
            log.error("Failed to load OpenAPI spec for MCP bridge", e);
        }
    }

    private record OperationDetails(String path, HttpMethod method, Operation operation) {}

    private void extractOperations(String path, HttpMethod method, Operation operation, Map<String, List<OperationDetails>> map) {
        if (operation == null || operation.getOperationId() == null) {
            return;
        }
        if (ignoredOperations.contains(operation.getOperationId())) {
            return;
        }

        String tag = "General";
        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            tag = operation.getTags().get(0);
        }
        
        map.computeIfAbsent(tag, k -> new ArrayList<>()).add(new OperationDetails(path, method, operation));
    }

    private void createCompositeToolForTag(String tag, List<OperationDetails> operations) {
        String toolName = "manage" + tag.replaceAll("\\s+", "");
        
        StringBuilder descriptionBuilder = new StringBuilder("Manage " + tag + ". Use this tool to interact with " + tag + " APIs. Supported operations (operationId):\n");
        List<String> operationIds = new ArrayList<>();
        
        for (OperationDetails detail : operations) {
            String opId = detail.operation().getOperationId();
            operationIds.add(opId);
            descriptionBuilder.append("- ").append(opId).append(": ").append(detail.operation().getSummary() != null ? detail.operation().getSummary() : detail.path()).append("\n");
        }

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> opIdSchema = new HashMap<>();
        opIdSchema.put("type", "string");
        opIdSchema.put("enum", operationIds);
        opIdSchema.put("description", "The operation to execute.");
        properties.put("operationId", opIdSchema);
        
        Map<String, Object> argsSchema = new HashMap<>();
        argsSchema.put("type", "object");
        argsSchema.put("description", "A JSON object containing required parameters for the chosen operation.");
        
        Map<String, Object> allArgsProperties = new HashMap<>();
        
        for (OperationDetails detail : operations) {
            Operation op = detail.operation();
            if (op.getParameters() != null) {
                for (var param : op.getParameters()) {
                    Map<String, Object> paramSchema = new HashMap<>();
                    if (param.getSchema() != null && param.getSchema().getType() != null) {
                        paramSchema.put("type", param.getSchema().getType());
                    } else {
                        paramSchema.put("type", "string");
                    }
                    paramSchema.put("description", (param.getDescription() != null ? param.getDescription() : "Parameter for " + op.getOperationId()));
                    allArgsProperties.put(param.getName(), paramSchema);
                }
            }
            if (op.getRequestBody() != null && op.getRequestBody().getContent() != null) {
                var jsonContent = op.getRequestBody().getContent().get("application/json");
                if (jsonContent != null && jsonContent.getSchema() != null) {
                    Map<String, Object> bodySchema = new HashMap<>();
                    bodySchema.put("type", "object");
                    bodySchema.put("description", "The JSON request body for " + op.getOperationId());
                    allArgsProperties.put("requestBody", bodySchema);
                }
            }
        }
        
        argsSchema.put("properties", allArgsProperties);
        properties.put("args", argsSchema);
        
        schema.put("properties", properties);
        schema.put("required", List.of("operationId"));
        
        String jsonSchema;
        try {
            jsonSchema = objectMapper.writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            jsonSchema = "{\"type\":\"object\"}";
        }

        Function<Map<String, Object>, Object> compositeApiCall = (input) -> {
            String operationId = (String) input.get("operationId");
            @SuppressWarnings("unchecked")
            Map<String, Object> args = (Map<String, Object>) input.getOrDefault("args", new HashMap<>());
            
            OperationDetails targetOp = operations.stream()
                .filter(op -> op.operation().getOperationId().equals(operationId))
                .findFirst()
                .orElse(null);
                
            if (targetOp == null) {
                return Map.of("error", "Unknown operationId: " + operationId);
            }
            
            return executeWebClientProxy(targetOp.path(), targetOp.method(), targetOp.operation(), args);
        };

        ToolCallback callback = FunctionToolCallback.builder(toolName, compositeApiCall)
                .description(descriptionBuilder.toString())
                .inputSchema(jsonSchema)
                .build();

        toolCallbacks.add(callback);
    }

    private Object executeWebClientProxy(String path, HttpMethod method, Operation operation, Map<String, Object> args) {
        String operationId = operation.getOperationId();
        try {
            log.info("MCP Tool '{}' called with args: {}", operationId, args);
            
            // 1. Build the URI with path variables and query params
            var uriBuilder = org.springframework.web.util.UriComponentsBuilder.fromPath(path);
            Map<String, Object> pathVariables = new HashMap<>();
            
            if (operation.getParameters() != null) {
                for (var param : operation.getParameters()) {
                    String paramName = param.getName();
                    if (args.containsKey(paramName)) {
                        if ("path".equals(param.getIn())) {
                            pathVariables.put(paramName, args.get(paramName));
                        } else if ("query".equals(param.getIn())) {
                            uriBuilder.queryParam(paramName, args.get(paramName));
                        }
                    }
                }
            }
            
            String finalUri = uriBuilder.buildAndExpand(pathVariables).toUriString();
            
            // 2. Prepare the WebClient request
            var request = webClient.method(method).uri(finalUri);
            
            // 3. Set Request Body
            if (args.containsKey("requestBody")) {
                request.bodyValue(args.get("requestBody"));
            }
            
            // 4. Forward Auth Token (Attempt to get from Reactor Context, fallback if needed)
            String token = org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(auth -> auth instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken)
                .map(auth -> ((org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken) auth).getToken().getTokenValue())
                .onErrorResume(e -> reactor.core.publisher.Mono.empty())
                .block();
                
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            } else {
                log.warn("No active JWT found in context for MCP tool {}, proceeding without auth forwarding", operationId);
            }
            
            // 5. Execute call
            String response = request.retrieve()
                .bodyToMono(String.class)
                .block();
                
            return Map.of(
                "status", "success",
                "operationId", operationId,
                "response", response != null ? response : "No content"
            );
        } catch (Exception e) {
            log.error("Failed to execute MCP tool {}", operationId, e);
            return Map.of("error", e.getMessage());
        }
    }

    private String buildJsonSchema(Operation operation) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();

        // 1. Process Path and Query Parameters
        if (operation.getParameters() != null) {
            for (var param : operation.getParameters()) {
                Map<String, Object> paramSchema = new HashMap<>();
                if (param.getSchema() != null && param.getSchema().getType() != null) {
                    paramSchema.put("type", param.getSchema().getType());
                } else {
                    paramSchema.put("type", "string");
                }
                
                if (param.getDescription() != null) {
                    paramSchema.put("description", param.getDescription());
                }
                
                properties.put(param.getName(), paramSchema);
                
                if (Boolean.TRUE.equals(param.getRequired())) {
                    required.add(param.getName());
                }
            }
        }

        // 2. Process Request Body (application/json)
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            var jsonContent = operation.getRequestBody().getContent().get("application/json");
            if (jsonContent != null && jsonContent.getSchema() != null) {
                // For simplicity in this bridge, we expect the LLM to provide the full body as a single 'requestBody' JSON object
                Map<String, Object> bodySchema = new HashMap<>();
                bodySchema.put("type", "object");
                bodySchema.put("description", "The JSON request body for this operation");
                properties.put("requestBody", bodySchema);
                
                if (Boolean.TRUE.equals(operation.getRequestBody().getRequired())) {
                    required.add("requestBody");
                }
            }
        }

        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        
        try {
            return objectMapper.writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize JSON schema for {}", operation.getOperationId(), e);
            return "{\"type\":\"object\"}";
        }
    }

    @Override
    public ToolCallback[] getToolCallbacks() {
        return toolCallbacks.toArray(ToolCallback[]::new);
    }
}

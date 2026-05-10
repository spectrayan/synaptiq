package com.spectrayan.synaptiq.agentflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spectrayan.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.spectrayan.synaptiq.agentflow.executor.FlowExecutor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
public class AgentEngineApp {

    private final FlowExecutor flowExecutor;
    private final ObjectMapper objectMapper;

    public AgentEngineApp() {
        this.flowExecutor = new FlowExecutor();
        this.objectMapper = new ObjectMapper();
    }

    public AgentEngineApp(FlowExecutor flowExecutor, ObjectMapper objectMapper) {
        this.flowExecutor = flowExecutor;
        this.objectMapper = objectMapper;
    }

    /**
     * Streams the query results by dynamically parsing the flow spec and delegating to FlowExecutor.
     * 
     * @param input Contains "workflow_spec" (the JSON spec) and "query" or other input parameters.
     * @return A Flux of event maps mimicking the Python stream yielding.
     */
    public Flux<Map<String, Object>> streamQuery(Map<String, Object> input) {
        if (!input.containsKey("workflow_spec")) {
            return Flux.error(new IllegalArgumentException("Missing 'workflow_spec' in input"));
        }

        try {
            // Parse spec
            Object specRaw = input.get("workflow_spec");
            FlowSettings spec;
            if (specRaw instanceof String) {
                spec = objectMapper.readValue((String) specRaw, FlowSettings.class);
            } else {
                spec = objectMapper.convertValue(specRaw, FlowSettings.class);
            }

            // Extract the user query/input
            Object userQuery = input.getOrDefault("query", input);

            // Start run
            String runId = flowExecutor.startRun(spec, userQuery);

            // Stream results
            return flowExecutor.streamRun(runId);

        } catch (Exception e) {
            log.error("Failed to parse flow spec or start run", e);
            return Flux.error(e);
        }
    }
}

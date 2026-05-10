package com.synaptiq.workflow.application.service;

import com.synaptiq.workflow.application.port.in.WorkflowGenerationUseCase;
import com.synaptiq.workflow.domain.model.FlowEvent;
import com.synaptiq.workflow.infrastructure.llm.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.Map;

/**
 * Generates workflow specs from natural-language prompts via Gemini LLM.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowGenerationService implements WorkflowGenerationUseCase {

    private final GeminiClient geminiClient;

    private static final String SYSTEM_PROMPT = """
        You are a workflow architect. Given a user's natural language description of a task,
        generate a JSON workflow spec with the following structure:
        {
          "name": "Workflow Name",
          "entrypoint": "first_agent_id",
          "flowType": "STATIC",
          "agents": [
            {
              "id": "agent_id",
              "name": "Agent Name",
              "systemPrompt": "Detailed system prompt for this agent",
              "description": "What this agent does",
              "tools": []
            }
          ],
          "edges": [
            { "source": "agent1_id", "target": "agent2_id" }
          ]
        }

        Rules:
        - Use snake_case for agent IDs
        - Write detailed, actionable system prompts for each agent
        - The last agent should have an edge to "END"
        - Create 2-5 agents that break the task into logical steps
        - Return ONLY the JSON, no markdown fences or explanation
        """;

    @Override
    public Flux<FlowEvent> generateWorkflow(String tenantId, String prompt) {
        log.info("[WorkflowGeneration] tenantId={}, prompt='{}'", tenantId, prompt);

        return Flux.create(sink -> {
            // Status event: generation starting
            sink.next(new FlowEvent.StepCompleted(
                "gen-" + System.currentTimeMillis(),
                "generate", "Generating workflow",
                "running",
                Map.of("message", "Analyzing prompt with Gemini..."),
                Instant.now()
            ));

            geminiClient.generateContent(SYSTEM_PROMPT, prompt)
                .subscribe(
                    result -> {
                        try {
                            // Try to parse the result as JSON
                            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            // Strip markdown fences if present
                            String cleaned = result.strip();
                            if (cleaned.startsWith("```")) {
                                cleaned = cleaned.replaceFirst("```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
                            }
                            @SuppressWarnings("unchecked")
                            Map<String, Object> spec = mapper.readValue(cleaned, Map.class);

                            sink.next(new FlowEvent.Completed(
                                "gen-" + System.currentTimeMillis(),
                                spec,
                                500,
                                Instant.now()
                            ));
                        } catch (Exception e) {
                            log.warn("[WorkflowGeneration] Failed to parse LLM response as JSON, using raw", e);
                            // Fallback: emit a simple 2-agent workflow
                            Map<String, Object> fallback = Map.of(
                                "name", "Generated: " + prompt.substring(0, Math.min(40, prompt.length())),
                                "entrypoint", "researcher",
                                "flowType", "STATIC",
                                "agents", java.util.List.of(
                                    Map.of("id", "researcher", "name", "Researcher",
                                        "systemPrompt", result, "tools", java.util.List.of()),
                                    Map.of("id", "writer", "name", "Writer",
                                        "systemPrompt", "Synthesize the research into a clear report.",
                                        "tools", java.util.List.of())
                                ),
                                "edges", java.util.List.of(
                                    Map.of("source", "researcher", "target", "writer"),
                                    Map.of("source", "writer", "target", "END")
                                )
                            );
                            sink.next(new FlowEvent.Completed(
                                "gen-" + System.currentTimeMillis(),
                                fallback, 500, Instant.now()
                            ));
                        }
                        sink.complete();
                    },
                    error -> {
                        sink.next(new FlowEvent.Failed(
                            "gen-" + System.currentTimeMillis(),
                            error.getMessage(), null, Instant.now()
                        ));
                        sink.complete();
                    }
                );
        });
    }
}

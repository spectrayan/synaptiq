package com.spectrayan.synaptiq.workflow.application.service;

import com.spectrayan.synaptiq.workflow.application.port.in.PromptRegenerationUseCase;
import com.spectrayan.synaptiq.workflow.infrastructure.llm.GeminiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Regenerates / improves a single agent node's system prompt using Gemini LLM.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromptRegenerationService implements PromptRegenerationUseCase {

    private final GeminiClient geminiClient;

    private static final String SYSTEM_PROMPT = """
        You are an expert prompt engineer. Your task is to improve or regenerate a system prompt
        for an AI agent node within a multi-agent workflow.

        Rules:
        - Write a clear, detailed, actionable system prompt
        - Include specific instructions, not vague platitudes
        - Structure with sections: Role, Instructions, Guidelines, Output Format
        - Keep the tone professional and precise
        - Return ONLY the improved prompt text, no explanations or markdown fences
        """;

    @Override
    public Mono<String> regeneratePrompt(
        String tenantId,
        String nodeId,
        String nodeLabel,
        String nodeDescription,
        String currentPrompt,
        String instruction,
        Object workflowContext
    ) {
        log.info("[RegeneratePrompt] tenantId={}, node={}, instruction='{}'", tenantId, nodeId, instruction);

        String userPrompt = String.format("""
            Agent Name: %s
            Agent Description: %s
            Current System Prompt:
            ---
            %s
            ---
            User Instruction: %s

            Please regenerate/improve the system prompt based on the user's instruction.
            """,
            nodeLabel != null ? nodeLabel : nodeId,
            nodeDescription != null ? nodeDescription : "",
            currentPrompt != null ? currentPrompt : "(empty)",
            instruction != null ? instruction : "Improve the prompt to be more detailed and effective"
        );

        return geminiClient.generateContent(SYSTEM_PROMPT, userPrompt)
            .map(result -> {
                // Strip markdown fences if present
                String cleaned = result.strip();
                if (cleaned.startsWith("```")) {
                    cleaned = cleaned.replaceFirst("```(?:markdown|text)?\\s*", "").replaceFirst("\\s*```$", "");
                }
                return cleaned;
            });
    }
}

package me.johnra.tutorial.agent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BugInvestigationAgent {

    private static final Logger log = LoggerFactory.getLogger(BugInvestigationAgent.class);

    private static final int MAX_ITERATIONS = 15;
    private static final int TOOL_CALL_BUDGET = 20;

    private static final String SYSTEM_PROMPT = """
            You are a senior software engineer specializing in debugging.

            Your job is to investigate a bug report thoroughly using the tools provided.
            You MUST use the tools — do not answer from memory or assumption.

            Follow this process:
            1. Call runTests (empty string for all tests) to see what is currently failing
            2. Call searchCodebase to find relevant source files
            3. Call readFile to read the suspicious code
            4. Form a hypothesis and propose a specific fix

            Be methodical. Follow the evidence from the tools.
            If you cannot find the root cause, say so — do NOT fabricate a fix.
            """;

    private final ChatLanguageModel chatModel;
    private final CodebaseTools tools;
    private final List<ToolSpecification> toolSpecs;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BugInvestigationAgent(ChatLanguageModel chatModel, CodebaseTools tools) {
        this.chatModel = chatModel;
        this.tools = tools;
        this.toolSpecs = ToolSpecifications.toolSpecificationsFrom(tools);
        log.info("Agent initialized with {} tool specs: {}",
                toolSpecs.size(),
                toolSpecs.stream().map(ToolSpecification::name).toList());
    }

    public String investigate(String bugDescription) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(SYSTEM_PROMPT));
        messages.add(UserMessage.from("Investigate this bug:\n\n" + bugDescription));

        int iteration = 0;
        int totalToolCalls = 0;

        log.info("Starting investigation...");

        while (iteration < MAX_ITERATIONS) {
            iteration++;
            log.info("--- Iteration {} ---", iteration);

            Response<AiMessage> response;
            try {
                response = chatModel.generate(messages, toolSpecs);
            } catch (Exception e) {
                log.error("Model call failed on iteration {}: {}", iteration, e.getMessage());
                return "Investigation failed: model error — " + e.getMessage();
            }

            AiMessage aiMessage = response.content();
            messages.add(aiMessage);

            log.info("Model response: hasToolCalls={}, text={}",
                    aiMessage.hasToolExecutionRequests(),
                    aiMessage.text() != null ? aiMessage.text().substring(0, Math.min(100, aiMessage.text().length())) : "(none)");

            if (!aiMessage.hasToolExecutionRequests()) {
                log.info("Investigation complete — {} iterations, {} tool calls", iteration, totalToolCalls);
                return aiMessage.text();
            }

            for (var request : aiMessage.toolExecutionRequests()) {
                if (++totalToolCalls > TOOL_CALL_BUDGET) {
                    return "Investigation halted: tool call budget of %d exceeded.".formatted(TOOL_CALL_BUDGET);
                }

                log.info("  Executing tool [{}/{}]: {} args={}",
                        totalToolCalls, TOOL_CALL_BUDGET, request.name(), request.arguments());

                String result;
                try {
                    result = executeTool(request.name(), request.arguments());
                } catch (Exception e) {
                    result = "Tool execution error: " + e.getMessage();
                    log.error("  Tool {} threw: {}", request.name(), e.getMessage());
                }

                log.info("  Tool result (first 200 chars): {}",
                        result.substring(0, Math.min(200, result.length())));

                messages.add(ToolExecutionResultMessage.from(request, result));
            }
        }

        return "Investigation halted: max iterations (%d) reached.".formatted(MAX_ITERATIONS);
    }

    private String executeTool(String name, String argumentsJson) {
        Map<String, Object> args = parseArgs(argumentsJson);
        return switch (name) {
            case "searchCodebase" -> tools.searchCodebase(
                    str(args, "query"),
                    str(args, "fileExtension"));
            case "readFile" -> tools.readFile(
                    str(args, "path"),
                    toInt(args.get("startLine")),
                    toInt(args.get("endLine")));
            case "runTests" -> tools.runTests(
                    str(args, "testPath"));
            default -> "Unknown tool: " + name;
        };
    }

    private Map<String, Object> parseArgs(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse tool arguments '{}': {}", json, e.getMessage());
            return new HashMap<>();
        }
    }

    private String str(Map<String, Object> args, String key) {
        Object v = args.get(key);
        return v == null ? "" : v.toString();
    }

    private int toInt(Object value) {
        if (value instanceof Number n) return n.intValue();
        return 0;
    }
}

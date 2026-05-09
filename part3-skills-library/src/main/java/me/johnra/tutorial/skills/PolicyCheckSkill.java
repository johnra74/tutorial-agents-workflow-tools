package me.johnra.tutorial.skills;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PolicyCheckSkill extends BaseSkill<PolicyCheckResult> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<String> rules;

    public PolicyCheckSkill(ChatLanguageModel chatModel, List<String> rules) {
        super(chatModel);
        this.rules = rules;
    }

    @Override
    public String skillName() {
        return "policy_check";
    }

    @Override
    protected String buildSystemPrompt() {
        String rulesText = IntStream.range(0, rules.size())
                .mapToObj(i -> (i + 1) + ". " + rules.get(i))
                .collect(Collectors.joining("\n"));

        return """
                You are a content policy reviewer. Check the input against these rules:
                %s

                Respond in JSON only using this exact format:
                {"passed": true|false, "violations": ["<rule>: <reason>", ...], "severity": "none|minor|major|critical"}

                violations must be an array of plain strings, never objects.

                Examples:
                - Clean content:   {"passed": true, "violations": [], "severity": "none"}
                - One violation:   {"passed": false, "violations": ["No competitor disparagement: content mocks a competitor by name"], "severity": "major"}
                - Two violations:  {"passed": false, "violations": ["Rule A: reason", "Rule B: reason"], "severity": "critical"}

                Severity guide:
                - none: no violations
                - minor: style issues, easily fixed
                - major: significant problems requiring rework
                - critical: harmful, illegal, or severely misleading content

                No explanation outside the JSON. No markdown. Raw JSON only.
                """.formatted(rulesText);
    }

    @Override
    protected PolicyCheckResult parseResponse(String text) throws Exception {
        return MAPPER.readValue(JsonExtractor.extract(text), PolicyCheckResult.class);
    }
}

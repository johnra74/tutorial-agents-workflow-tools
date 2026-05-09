package me.johnra.tutorial.skills;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.util.List;
import java.util.stream.Collectors;

public class ClassifySkill extends BaseSkill<ClassificationResult> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<String> categories;

    public ClassifySkill(ChatLanguageModel chatModel, List<String> categories) {
        super(chatModel);
        this.categories = categories;
    }

    @Override
    public String skillName() {
        return "classify";
    }

    @Override
    protected String buildSystemPrompt() {
        String categoryList = categories.stream()
                .map(c -> "- " + c)
                .collect(Collectors.joining("\n"));

        return """
                You are a text classifier. Classify the input into exactly one of these categories:
                %s

                Respond in JSON only:
                {"category": "<one of the categories above>", "confidence": <0.0 to 1.0>, "reasoning": "<one sentence>"}
                No explanation. No markdown. Raw JSON only.
                """.formatted(categoryList);
    }

    @Override
    protected ClassificationResult parseResponse(String text) throws Exception {
        ClassificationResult result = MAPPER.readValue(JsonExtractor.extract(text), ClassificationResult.class);
        if (!categories.contains(result.category())) {
            throw new IllegalArgumentException("Model returned unknown category: " + result.category());
        }
        return result;
    }
}

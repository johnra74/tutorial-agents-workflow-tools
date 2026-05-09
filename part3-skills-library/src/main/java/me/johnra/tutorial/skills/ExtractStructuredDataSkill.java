package me.johnra.tutorial.skills;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ExtractStructuredDataSkill<T> extends BaseSkill<T> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Class<T> schema;

    public ExtractStructuredDataSkill(ChatLanguageModel chatModel, Class<T> schema) {
        super(chatModel);
        this.schema = schema;
    }

    @Override
    public String skillName() {
        return "extract_" + schema.getSimpleName().toLowerCase();
    }

    @Override
    protected String buildSystemPrompt() {
        String fields = describeFields();
        return """
                You are a data extraction assistant. Extract information from the input and return it as JSON.

                Required JSON format:
                {
                %s
                }

                Return only valid JSON matching this format. No explanation, no preamble, no markdown fences.
                """.formatted(fields);
    }

    @Override
    protected T parseResponse(String text) throws Exception {
        return MAPPER.readValue(JsonExtractor.extract(text), schema);
    }

    private String describeFields() {
        // Use record components if available (Java 16+), fall back to declared fields
        if (schema.isRecord()) {
            return Arrays.stream(schema.getRecordComponents())
                    .map(c -> "  \"%s\": <%s>".formatted(c.getName(), c.getType().getSimpleName()))
                    .collect(Collectors.joining(",\n"));
        }
        return Arrays.stream(schema.getDeclaredFields())
                .filter(f -> !f.isSynthetic())
                .map(f -> "  \"%s\": <%s>".formatted(f.getName(), f.getType().getSimpleName()))
                .collect(Collectors.joining(",\n"));
    }
}

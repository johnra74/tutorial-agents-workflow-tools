package me.johnra.tutorial.skills;

import dev.langchain4j.model.chat.ChatLanguageModel;

public class SummarizeSkill extends BaseSkill<String> {

    private final int targetWords;
    private final String style;

    public SummarizeSkill(ChatLanguageModel chatModel, int targetWords, String style) {
        super(chatModel);
        this.targetWords = targetWords;
        this.style = style;
    }

    public SummarizeSkill(ChatLanguageModel chatModel) {
        this(chatModel, 150, "neutral");
    }

    @Override
    public String skillName() {
        return "summarize";
    }

    @Override
    protected String buildSystemPrompt() {
        return """
                You are an expert summarizer. Summarize the input in approximately %d words.
                Style: %s
                Return only the summary — no preamble, no "Here is a summary of...", no meta-commentary.
                """.formatted(targetWords, style);
    }

    @Override
    protected String parseResponse(String text) {
        return text.strip();
    }
}

package me.johnra.tutorial.skills;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class BaseSkill<T> implements Skill<T> {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final ChatLanguageModel chatModel;
    private final int maxRetries;

    protected BaseSkill(ChatLanguageModel chatModel) {
        this(chatModel, 3);
    }

    protected BaseSkill(ChatLanguageModel chatModel, int maxRetries) {
        this.chatModel = chatModel;
        this.maxRetries = maxRetries;
    }

    protected abstract String buildSystemPrompt();

    protected abstract T parseResponse(String text) throws Exception;

    @Override
    public SkillResult<T> run(String input) {
        Exception lastError = null;
        long start = System.currentTimeMillis();

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                List<ChatMessage> messages = List.of(
                        SystemMessage.from(buildSystemPrompt()),
                        UserMessage.from(input));

                Response<AiMessage> response = chatModel.generate(messages);
                long latencyMs = System.currentTimeMillis() - start;

                T output = parseResponse(response.content().text());

                TokenUsage usage = response.tokenUsage();
                int inputTokens = usage != null ? usage.inputTokenCount() : 0;
                int outputTokens = usage != null ? usage.outputTokenCount() : 0;

                log.info("[{}] success | tokens={}/{} | latency={}ms",
                        skillName(), inputTokens, outputTokens, latencyMs);

                return new SkillResult<>(output, "ollama", inputTokens, outputTokens, latencyMs, skillName());

            } catch (Exception e) {
                lastError = e;
                if (attempt < maxRetries - 1) {
                    long waitMs = (long) Math.pow(2, attempt) * 1000L;
                    log.warn("[{}] attempt {} failed, retrying in {}ms: {}",
                            skillName(), attempt + 1, waitMs, e.getMessage());
                    try {
                        Thread.sleep(waitMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        throw new RuntimeException(
                "[" + skillName() + "] failed after " + maxRetries + " attempts", lastError);
    }
}

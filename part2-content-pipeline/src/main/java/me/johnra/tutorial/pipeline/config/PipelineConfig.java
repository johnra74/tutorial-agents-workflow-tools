package me.johnra.tutorial.pipeline.config;

import me.johnra.tutorial.pipeline.service.*;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class PipelineConfig {

    @Value("${ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${ollama.model:llama3.2}")
    private String modelName;

    @Bean
    public ChatLanguageModel chatModel() {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.0)
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    @Bean
    public ContentClassifier contentClassifier(ChatLanguageModel model) {
        return AiServices.create(ContentClassifier.class, model);
    }

    @Bean
    public PolicyComplianceChecker policyComplianceChecker(ChatLanguageModel model) {
        return AiServices.create(PolicyComplianceChecker.class, model);
    }

    @Bean
    public ReadabilityScorer readabilityScorer(ChatLanguageModel model) {
        return AiServices.create(ReadabilityScorer.class, model);
    }

    @Bean
    public EditorialBriefWriter editorialBriefWriter(ChatLanguageModel model) {
        return AiServices.create(EditorialBriefWriter.class, model);
    }

    // Java 21 virtual threads — one per parallel pipeline step, cheap and non-blocking
    @Bean(name = "pipelineExecutor")
    public Executor pipelineExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}

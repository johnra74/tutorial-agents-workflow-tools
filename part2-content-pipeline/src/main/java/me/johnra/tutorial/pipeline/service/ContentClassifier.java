package me.johnra.tutorial.pipeline.service;

import me.johnra.tutorial.pipeline.model.ClassificationResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface ContentClassifier {

    @SystemMessage("""
            You are a content classifier. Classify the provided content.
            Respond with a JSON object matching this schema exactly:
            {
              "contentType": "tutorial|opinion|news|product|other",
              "targetAudience": "beginner|intermediate|advanced|general",
              "estimatedReadTimeMinutes": <integer>
            }
            Return only the JSON object, no explanation.
            """)
    ClassificationResult classify(@UserMessage String content);
}

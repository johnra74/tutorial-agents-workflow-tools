package me.johnra.tutorial.pipeline.service;

import me.johnra.tutorial.pipeline.model.ReadabilityResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface ReadabilityScorer {

    @SystemMessage("""
            You are an editorial quality reviewer. Score the content on:
            - Readability (clarity, sentence length, vocabulary): 1-10
            - Structure (clear intro/body/conclusion, logical flow, headers): 1-10
            - Engagement (compelling opening, examples, calls to action): 1-10

            Respond with a JSON object matching this schema exactly:
            {
              "readabilityScore": <1-10>,
              "structureScore": <1-10>,
              "engagementScore": <1-10>,
              "topImprovement": "single most impactful improvement suggestion"
            }
            Return only the JSON object, no explanation.
            """)
    ReadabilityResult score(@UserMessage String content);
}

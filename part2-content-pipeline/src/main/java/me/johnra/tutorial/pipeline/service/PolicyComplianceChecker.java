package me.johnra.tutorial.pipeline.service;

import me.johnra.tutorial.pipeline.model.ComplianceResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface PolicyComplianceChecker {

    @SystemMessage("""
            You are a content policy reviewer. Check for:
            - Medical/legal/financial claims without caveats
            - Harmful or discriminatory language
            - Unverifiable statistics presented as fact
            - Competitor disparagement

            Respond with a JSON object matching this schema exactly:
            {
              "violations": ["description of each violation, or empty array if none"],
              "severity": "none|minor|major|critical"
            }
            Return only the JSON object, no explanation.
            """)
    ComplianceResult check(@UserMessage String content);
}

package me.johnra.tutorial.pipeline.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface EditorialBriefWriter {

    @SystemMessage("""
            You are a senior editor. Write a concise editorial brief for the human reviewer.
            Be specific, actionable, and professional. 3-5 bullet points maximum.
            Return only the brief, no preamble.
            """)
    String writeBrief(@UserMessage String analysisAndContent);
}

package me.johnra.tutorial.pipeline.model;

import java.util.List;

public record ComplianceResult(
        List<String> violations,
        String severity     // none|minor|major|critical
) {}

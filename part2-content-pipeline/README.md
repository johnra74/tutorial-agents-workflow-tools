# Part 2 — Content Quality Pipeline

Companion code for **[Part 2: Workflows — When You Know the Steps Before You Start](https://www.johnra.me/2026/05/06/part-2-workflow-when-you-know-the-steps-before-you-start/)**.

## Purpose

Demonstrates how to build a deterministic AI workflow: a fixed sequence of steps where the path is known upfront and the AI handles the work at each individual step, but the overall execution order never changes based on model output.

## Objective

Given a piece of draft content, the pipeline runs five analysis steps and produces a structured report:

1. **Classify** — determine content type and topic
2. **Compliance check** — flag policy violations or sensitive material
3. **Readability score** — assess reading level and clarity
4. **Route** — decide the editorial destination based on classification and compliance
5. **Editorial brief** — generate a short editor-facing summary of findings

Steps 1–4 run in parallel using virtual threads. Step 5 waits for all prior results before generating the brief.

## Key Concepts

- **Workflow vs agent** — the execution path is fixed regardless of what the model returns; no model output determines which step runs next
- **Structured AI responses** — each step asks the model for JSON and deserializes it into a Java record using Jackson
- **Virtual threads** — `Executors.newVirtualThreadPerTaskExecutor()` runs the parallel analysis steps concurrently without blocking OS threads
- **`AiServices` proxy** — LangChain4j generates interface implementations at runtime; each service method is a single model call returning a typed result

## Project Structure

```
src/main/java/me/johnra/tutorial/pipeline/
├── ContentPipelineApplication.java        # Entry point; submits sample content and prints results
├── config/
│   └── PipelineConfig.java                # Configures OllamaChatModel and AiServices beans
├── model/
│   ├── ClassificationResult.java          # Record: contentType, topics, confidence
│   ├── ComplianceResult.java              # Record: passed, violations, severity
│   ├── ReadabilityResult.java             # Record: score, grade, suggestions
│   ├── RouteDecision.java                 # Record: destination, priority, reasoning
│   └── ContentAnalysis.java              # Aggregate record holding all step results
└── service/
    ├── ContentPipelineService.java        # Orchestrates the workflow; parallel + sequential steps
    ├── ContentClassifier.java             # AiServices interface: classify(content)
    ├── PolicyComplianceChecker.java       # AiServices interface: check(content)
    ├── ReadabilityScorer.java             # AiServices interface: score(content)
    ├── ContentRouter.java                 # AiServices interface: route(classification, compliance)
    └── EditorialBriefWriter.java          # AiServices interface: write(analysis)
```

## Configuration

`src/main/resources/application.properties`:

```properties
ollama.base-url=http://localhost:11434
ollama.model=llama3.1
```

Pull the model with `ollama pull llama3.1`.

## Running

```bash
mvn spring-boot:run
```

The pipeline processes a built-in sample article and prints the full `ContentAnalysis` result to stdout.

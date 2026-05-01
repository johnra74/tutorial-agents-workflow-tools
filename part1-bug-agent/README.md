# Part 1 — Bug Investigation Agent

Companion code for Part 1: Agents - When the Problem Doesn't Have a Roadmap.

## Purpose

Demonstrates how to build an AI agent that autonomously investigates a bug report. The agent reasons about what to look at, calls tools to gather evidence, and adapts its plan based on what it finds - without a human scripting each step.

## Objective

Given a natural-language bug report, the agent must:

1. Run the test suite to identify failing tests
2. Search the codebase for the relevant source files
3. Read and analyze the suspicious code
4. Identify the root cause and propose a specific fix

The agent targets `part1-calculator-target` - a deliberately buggy calculator project where `add()` subtracts instead of adds due to a copy-paste error.

## Key Concepts

- **Manual tool-calling loop** - `chatModel.generate(messages, toolSpecs)` drives the agent; each tool result is appended to the message list and the model is called again until it stops requesting tools
- **`@Tool` / `@P` annotations** - LangChain4j scans `CodebaseTools` at startup to build tool specifications the model can call by name
- **Guardrails** - an iteration cap (`MAX_ITERATIONS = 15`) and a tool call budget (`TOOL_CALL_BUDGET = 20`) prevent runaway loops
- **Explicit system prompt** - instructs the model to use tools rather than reason from memory; the numbered steps guide it toward the right investigation order

## Project Structure

```
src/main/java/me/johnra/tutorial/agent/
├── BugInvestigationApplication.java   # CommandLineRunner entry point; submits the bug report
├── BugInvestigationAgent.java         # Agent loop, guardrails, tool dispatch
└── CodebaseTools.java                 # Tool implementations: searchCodebase, readFile, runTests
```

## Configuration

`src/main/resources/application.properties`:

```properties
langchain4j.ollama.chat-model.base-url=http://localhost:11434
langchain4j.ollama.chat-model.model-name=qwen3
langchain4j.ollama.chat-model.temperature=0.0
langchain4j.ollama.chat-model.timeout=PT120S

# Path to the project the agent will investigate
project.root=../part1-calculator-target
```

The model must support tool calling. `qwen3` is recommended for this tutorial; pull it with `ollama pull qwen3`.

## Running

```bash
mvn spring-boot:run
```

The agent prints structured logs during investigation and outputs a final report to stdout.

# Part 1 — Calculator Target (Buggy)

Investigation target for Part 1: Agents - When the Problem Doesn't Have a Roadmap.

## Purpose

A simple REST calculator API with an intentional bug. This project exists to give `part1-bug-agent` a realistic codebase to investigate — one where the failing tests, the bug location, and the fix are all discoverable through code reading and test execution.

## Objective

The bug agent pointed at this project should be able to:

1. Run the test suite and observe 4 failing tests
2. Search the source code for `CalculatorService`
3. Read the `performOperation` method and spot the defect
4. Report the root cause and the one-line fix

## The Bug

In `CalculatorService.performOperation()`, the `ADD` case was copy-pasted from `SUBTRACT` and never corrected:

```java
case ADD      -> a - b;   // should be a + b
case SUBTRACT -> a - b;
```

This causes `add(2, 3)` to return `-1` instead of `5`. The bug is non-obvious because `add(7, 0)` returns the correct result (`7 - 0 = 7`), making it appear as though addition works for some inputs.

## Failing Tests

| Test | Expected | Actual |
|---|---|---|
| `add_two_positive_numbers` (2+3) | 5 | -1 |
| `add_positive_and_negative` (4+(-3)) | 1 | 7 |
| `add_two_negatives` ((-2)+(-3)) | -5 | 1 |
| `calculate_addition_request` | result=5, expr="2 + 3 = 5" | result=-1 |

`add_zero_to_number` (7+0) passes because subtraction of zero produces the same result.

## Project Structure

```
src/
├── main/java/me/johnra/tutorial/calculator/
│   ├── CalculatorApplication.java    # Spring Boot entry point
│   ├── CalculatorController.java     # REST endpoints
│   ├── CalculatorService.java        # Business logic — contains the bug
│   ├── CalculationRequest.java       # Request record (operation, a, b)
│   ├── CalculationResult.java        # Result record (result, expression)
│   └── Operation.java                # Enum: ADD, SUBTRACT, MULTIPLY, DIVIDE
└── test/java/me/johnra/tutorial/calculator/
    └── CalculatorServiceTest.java    # Unit tests; 4 fail due to the bug
```

## Running

```bash
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. To run the tests and see the failures:

```bash
mvn test
```

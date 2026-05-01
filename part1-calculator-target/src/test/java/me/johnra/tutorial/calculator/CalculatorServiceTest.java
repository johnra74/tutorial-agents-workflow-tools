package me.johnra.tutorial.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorServiceTest {

    private CalculatorService service;

    @BeforeEach
    void setUp() {
        service = new CalculatorService();
    }

    // --- Addition tests (3 fail, 1 passes — the add-zero edge case masks the bug) ---

    @Test
    void add_two_positive_numbers() {
        // 2 + 3 should be 5, but returns -1 due to the bug
        assertEquals(5, service.add(2, 3));
    }

    @Test
    void add_positive_and_negative() {
        // 4 + (-3) should be 1, but returns 7 due to the bug (4 - (-3) = 7)
        assertEquals(1, service.add(4, -3));
    }

    @Test
    void add_two_negatives() {
        // (-2) + (-3) should be -5, but returns 1 due to the bug (-2 - (-3) = 1)
        assertEquals(-5, service.add(-2, -3));
    }

    @Test
    void add_zero_to_number() {
        // 7 + 0 = 7 and 7 - 0 = 7 — this test passes, which obscures the bug
        assertEquals(7, service.add(7, 0));
    }

    // --- Subtraction tests (all pass) ---

    @Test
    void subtract_two_numbers() {
        assertEquals(3, service.subtract(5, 2));
    }

    @Test
    void subtract_to_negative() {
        assertEquals(-2, service.subtract(3, 5));
    }

    // --- Multiplication tests (all pass) ---

    @Test
    void multiply_two_numbers() {
        assertEquals(12, service.multiply(3, 4));
    }

    @Test
    void multiply_by_zero() {
        assertEquals(0, service.multiply(99, 0));
    }

    // --- Division tests (all pass) ---

    @Test
    void divide_two_numbers() {
        assertEquals(4, service.divide(12, 3));
    }

    @Test
    void divide_by_zero_throws() {
        assertThrows(ArithmeticException.class, () -> service.divide(10, 0));
    }

    // --- calculate() integration ---

    @Test
    void calculate_subtraction_request() {
        CalculationResult result = service.calculate(new CalculationRequest(Operation.SUBTRACT, 10, 4));
        assertEquals(6, result.result());
        assertEquals("10 - 4 = 6", result.expression());
    }

    @Test
    void calculate_addition_request() {
        // This fails — result is 2-3 = -1 instead of 2+3 = 5
        // The expression string also shows the wrong result
        CalculationResult result = service.calculate(new CalculationRequest(Operation.ADD, 2, 3));
        assertEquals(5, result.result());
        assertEquals("2 + 3 = 5", result.expression());
    }
}

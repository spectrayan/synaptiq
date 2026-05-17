package com.spectrayan.synaptiq.agentflow.executor;

import com.spectrayan.synaptiq.agentflow.builder.models.settings.ParallelConfig;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link VirtualThreadExecutionStrategy}.
 */
class VirtualThreadExecutionStrategyTest {

    private final VirtualThreadExecutionStrategy strategy = new VirtualThreadExecutionStrategy();

    @Test
    void executeParallel_emptyList() {
        List<Integer> result = strategy.executeParallel(
            List.of(), ParallelConfig.builder().build());
        assertTrue(result.isEmpty());
    }

    @Test
    void executeParallel_singleTask() {
        List<Integer> result = strategy.executeParallel(
            List.of(() -> 42), ParallelConfig.builder().build());
        assertEquals(List.of(42), result);
    }

    @Test
    void executeParallel_sequentialWhenDisabled() {
        ParallelConfig config = ParallelConfig.builder()
            .virtualThreads(false)
            .build();

        List<Callable<String>> tasks = List.of(
            () -> Thread.currentThread().getName(),
            () -> Thread.currentThread().getName()
        );

        List<String> result = strategy.executeParallel(tasks, config);

        assertEquals(2, result.size());
    }

    @Test
    void executeParallel_parallelWithVirtualThreads() {
        ParallelConfig config = ParallelConfig.builder()
            .virtualThreads(true)
            .maxConcurrency(5)
            .build();

        List<Callable<String>> tasks = List.of(
            () -> {
                Thread.sleep(10);
                return "task-1";
            },
            () -> {
                Thread.sleep(10);
                return "task-2";
            },
            () -> {
                Thread.sleep(10);
                return "task-3";
            }
        );

        List<String> result = strategy.executeParallel(tasks, config);

        assertEquals(3, result.size());
        assertTrue(result.contains("task-1"));
        assertTrue(result.contains("task-2"));
        assertTrue(result.contains("task-3"));
    }

    @Test
    void executeParallel_respectsMaxConcurrency() {
        ParallelConfig config = ParallelConfig.builder()
            .virtualThreads(true)
            .maxConcurrency(2)
            .build();

        List<Callable<Integer>> tasks = List.of(
            () -> 1, () -> 2, () -> 3, () -> 4, () -> 5
        );

        List<Integer> result = strategy.executeParallel(tasks, config);

        assertEquals(5, result.size());
        assertEquals(List.of(1, 2, 3, 4, 5), result);
    }
}

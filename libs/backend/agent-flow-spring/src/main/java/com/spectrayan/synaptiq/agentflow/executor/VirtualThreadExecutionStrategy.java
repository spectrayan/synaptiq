package com.spectrayan.synaptiq.agentflow.executor;

import com.spectrayan.synaptiq.agentflow.builder.models.settings.ParallelConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * Execution strategy that leverages Java 21+ virtual threads for
 * parallel agent, tool, and sub-flow execution.
 * <p>
 * When virtual threads are enabled, tasks are submitted to a
 * virtual-thread-per-task executor with a configurable concurrency
 * limit enforced via a {@link Semaphore}. When disabled, tasks
 * execute sequentially on the calling thread.
 */
@Slf4j
public class VirtualThreadExecutionStrategy {

    /**
     * Execute multiple tasks in parallel using virtual threads (if enabled)
     * or sequentially (if disabled).
     *
     * @param tasks  the list of callable tasks to execute
     * @param config parallel execution configuration
     * @param <T>    the result type
     * @return list of results in the same order as the input tasks
     */
    public <T> List<T> executeParallel(List<Callable<T>> tasks, ParallelConfig config) {
        if (tasks.isEmpty()) {
            return List.of();
        }

        if (!config.isVirtualThreads() || tasks.size() == 1) {
            return executeSequentially(tasks);
        }

        return executeWithVirtualThreads(tasks, config.getMaxConcurrency());
    }

    private <T> List<T> executeSequentially(List<Callable<T>> tasks) {
        List<T> results = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            try {
                results.add(task.call());
            } catch (Exception e) {
                throw new RuntimeException("Sequential task execution failed", e);
            }
        }
        return results;
    }

    private <T> List<T> executeWithVirtualThreads(List<Callable<T>> tasks, int maxConcurrency) {
        log.debug("Executing {} tasks in parallel with max concurrency {}",
            tasks.size(), maxConcurrency);

        Semaphore semaphore = new Semaphore(maxConcurrency);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<T>> futures = new ArrayList<>(tasks.size());

            for (Callable<T> task : tasks) {
                futures.add(executor.submit(() -> {
                    semaphore.acquire();
                    try {
                        return task.call();
                    } finally {
                        semaphore.release();
                    }
                }));
            }

            List<T> results = new ArrayList<>(tasks.size());
            for (Future<T> future : futures) {
                try {
                    results.add(future.get());
                } catch (Exception e) {
                    throw new RuntimeException("Parallel task execution failed", e);
                }
            }
            return results;
        }
    }
}

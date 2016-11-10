package main.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class KeyedThrottler<T> {
    private final static Logger logger = LoggerFactory.getLogger(KeyedThrottler.class.getCanonicalName());
    private final Queue<CallableTask<T>> queue;
    private final ExecutorService executorService;
    private final Map<String, CallableTask<T>> taskMap;
    private final AtomicInteger maxNumberOfConcurrentRequests;
    private final AtomicInteger currentNumberOfScheduledRequest;

    public KeyedThrottler(final int maxNumberOfConcurrentRequests, final ExecutorService executorService) {
        this.maxNumberOfConcurrentRequests = new AtomicInteger(maxNumberOfConcurrentRequests);
        this.currentNumberOfScheduledRequest = new AtomicInteger(0);
        this.executorService = executorService;
        this.queue = new ConcurrentLinkedQueue<>();
        this.taskMap = new ConcurrentHashMap<>();
    }

    public CompletableFuture<T> execute(final Callable<CompletableFuture<T>> callable, final String uniqueTaskId) {
        CallableTask<T> task = taskMap.get(uniqueTaskId);
        if (task != null) {
            logger.debug("task exists in throttler, id :{}", uniqueTaskId);
            return task.responseFuture;
        } else {
            CompletableFuture<T> future = new CompletableFuture<>();
            CallableTask<T> callableTask = new CallableTask<>(callable, future, uniqueTaskId);
            queue.add(callableTask);
            taskMap.put(uniqueTaskId, callableTask);
            logger.debug("task added in throttler, id :{}, queue size: {}", uniqueTaskId, queue.size());
            scheduleOnExecutor();
            return future;
        }
    }

    private Runnable scheduleIfApplicable() {
        return () -> {
            if (!queue.isEmpty()) {
                if (currentNumberOfScheduledRequest.get() < maxNumberOfConcurrentRequests.get()) {
                    CallableTask<T> task = queue.remove();
                    currentNumberOfScheduledRequest.incrementAndGet();
                    try {
                        logger.debug("task scheduled in throttler, id :{}", task.uniqueTaskId);
                        task.callable
                                .call()
                                .thenAccept(f -> {
                                    taskMap.remove(task.uniqueTaskId);
                                    logger.debug("task completed (success) in throttler, id :{}", task.uniqueTaskId);
                                    currentNumberOfScheduledRequest.decrementAndGet();
                                    task.responseFuture.complete(f);
                                    scheduleOnExecutor();
                                })
                                .exceptionally(throwable -> {
                                    onError(task, throwable);
                                    return null;
                                });
                    } catch (Exception e) {
                        onError(task, e);
                    }
                } else {
                    logger.info("Queue is full unable to schedule task number: {}", currentNumberOfScheduledRequest.get());
                }
            }
        };
    }

    private void onError(final CallableTask<T> task, final Throwable th) {
        logger.debug("task completed (failure) in throttler, id :{}", task.uniqueTaskId);
        taskMap.remove(task.uniqueTaskId);
        currentNumberOfScheduledRequest.decrementAndGet();
        task.responseFuture.completeExceptionally(th);
        scheduleOnExecutor();
    }

    private void scheduleOnExecutor() {
        executorService.submit(scheduleIfApplicable());
    }

    private static class CallableTask<T> {
        private final Callable<CompletableFuture<T>> callable;
        private final CompletableFuture<T> responseFuture;
        private final String uniqueTaskId;

        private CallableTask(final Callable<CompletableFuture<T>> callable,
                             final CompletableFuture<T> responseFuture, final String uniqueTaskId) {
            this.callable = callable;
            this.responseFuture = responseFuture;
            this.uniqueTaskId = uniqueTaskId;
        }
    }
}
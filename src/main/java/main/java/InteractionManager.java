package main.java;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class InteractionManager {

    private final ListeningExecutorService listeningExecutorService;

    public InteractionManager() {
        this.listeningExecutorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    }

    public static void main(String[] args) throws Exception {
        final List<CompletableFuture<String>> resultList = new ArrayList<>();
        final InteractionManager interactionManager = new InteractionManager();
        for (int i = 0; i < 12; i++) {
            resultList.add(interactionManager.networkCommand().run());
            interactionManager.shutdown();
        }
        FutureUtils.allOf(resultList).thenAccept(results -> results.forEach(System.out::println));
    }

    private void shutdown() {
        listeningExecutorService.shutdown();
    }

    private NetworkCommand<String> networkCommand() {
        return new NetworkCommand<>(listeningExecutorService.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Starting task" + threadName);
            Thread.sleep(5000);
            System.out.println("Done with task" + threadName);
            return threadName;
        }), "mine");
    }
}
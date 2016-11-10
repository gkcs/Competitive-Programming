package main.java;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

/**
 * Created by kalpesh.p on 3/7/16.
 */
public class FutureGround {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        new FutureGround().createFuture().get();
    }

    public CompletableFuture<Integer> createFuture() throws InterruptedException {
        CompletableFuture<Integer> futureMine = httpCall();
        CompletableFuture<String> tom = futureMine.thenApply(__ -> "tom");
        for (int i = 0; i < 100; i++) {
            final int finalI = i;
//            sleep(1);
            tom.thenApply(s -> makeBigDatabaseCall(finalI));
        }
        System.out.println("Hello");
        return futureMine;
    }

    private CompletableFuture<Integer> httpCall() {
        return CompletableFuture.supplyAsync(() -> {
//            System.out.println("Http Thread:" + Thread.currentThread().getName());
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 44;
        }, executorService);
    }

    private int makeBigDatabaseCall(Integer s) {
        System.out.println("Database: " + Thread.currentThread().getName() + " " + s);
        return s + 20;
    }
}
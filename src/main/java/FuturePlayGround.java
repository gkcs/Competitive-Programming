package main.java;

import java.util.concurrent.*;

public class FuturePlayGround {

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);


    public CompletableFuture<Integer> createFuture() throws InterruptedException {
        CompletableFuture<Integer> futureMine = httpCall();
        CompletableFuture<String> tom = futureMine.thenApply(__ -> "tom");
        for (int i = 1; i < 11; i++) {
            int finalI = i;
            tom.thenApply(s -> makeBigDatabaseCall(finalI));
//            Thread.sleep(1);
        }

        return futureMine;
    }

    private int makeBigDatabaseCall(Integer s) {
        System.out.println("Database: " + Thread.currentThread().getName() + " " + s);
        return s + 20;
    }

    private CompletableFuture<Integer> httpCall() {
        return CompletableFuture.supplyAsync(() -> {
//            System.out.println("Http Thread:" + Thread.currentThread().getName());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 44;
        }, executorService);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println(new FuturePlayGround().createFuture().get());
    }
}

package main.java;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import rx.Observable;
import rx.internal.producers.SingleDelayedProducer;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

public class FutureUtils {

    public static <T> Observable<T> from(final ListenableFuture<T> future) {
        return rx.Observable.create(subscriber -> {
            final SingleDelayedProducer<T> sdp = new SingleDelayedProducer<>(subscriber);
            subscriber.setProducer(sdp);
            Futures.addCallback(future, new FutureCallback<T>() {
                @Override
                public void onSuccess(T result) {
                    sdp.setValue(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    subscriber.onError(t);
                }
            });
        });
    }

    public static <T> CompletableFuture<List<T>> allOf(Collection<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(toList()));
    }

    public static <T> CompletableFuture<T> from(final Observable<T> observable) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        observable.doOnError(future::completeExceptionally).takeFirst(future::complete);
        return future;
    }
}
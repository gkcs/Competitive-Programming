package main.java;

import com.google.common.util.concurrent.ListenableFuture;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixObservableCommand;
import rx.Observable;

import java.util.concurrent.CompletableFuture;

public class NetworkCommand<RESULT> extends HystrixObservableCommand<RESULT> {

    private final ListenableFuture<RESULT> future;

    public NetworkCommand(final ListenableFuture<RESULT> future,
                          final String key) {
        super(HystrixCommandGroupKey.Factory.asKey(key));
        this.future = future;
    }

    public CompletableFuture<RESULT> run() throws Exception {
        return FutureUtils.from(observe());
    }

    @Override
    protected Observable<RESULT> construct() {
        return FutureUtils.from(future);
    }
}
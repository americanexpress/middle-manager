package io.aexp.concurrency.middlemanager;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class ExecutorServiceWorkExecutor extends WorkExecutorBase {

    private final ExecutorService executorService;

    public ExecutorServiceWorkExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Nonnull
    @Override
    public <T extends Runnable> Future<T> submit(T runnable) {
        return executorService.submit(runnable, runnable);
    }

    @Nonnull
    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return executorService.submit(callable);
    }
}

package io.aexp.concurrency.middlemanager;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Exposes {@link java.util.concurrent.Executor}-like functionality (using Futures, Runnables, etc.) in a way similar to
 * {@code WorkManager}. It is intended to be a stepping stone to migrate code away from WorkManager.
 */
@ThreadSafe
public interface WorkExecutor {

    /**
     * Submit a runnable for execution. The resulting future will return the original Runnable from {@link Future#get()}
     * on successful completion, as this is analogous to how a WorkItem lets you extract the original Work from it.
     *
     * @param runnable runnable to execute
     * @param <T>      type of Runnable
     * @return a future representing the computation
     * @see java.util.concurrent.ExecutorService#submit(Runnable)
     */
    @Nonnull
    <T extends Runnable> Future<T> submit(T runnable);

    /**
     * Submit a callable for execution. The resulting future will return the value produced by {@link Callable#call()}
     * from {@link Future#get()} upon successful completion.
     *
     * @param callable callable to execute
     * @param <T>      type that callable will emit
     * @return a future representing the computation
     * @see java.util.concurrent.ExecutorService#submit(Callable)
     */
    @Nonnull
    <T> Future<T> submit(Callable<T> callable);

    /**
     * Wait for the future to complete by any means (exceptional or not). Analog to WorkManager#join.
     *
     * @param f       future
     * @param timeout timeout
     * @param unit    unit of timeout
     * @return true if the future completed (possibly unsuccessfully) by timeout
     */
    boolean waitForCompletion(Future<?> f, long timeout, TimeUnit unit);

    /**
     * Wait for any of the futures to complete (exceptionally or not).
     *
     * @param futures list of futures
     * @param timeout timeout amount
     * @param unit    timeout unit
     * @return true if any of the futures complete within the timeout
     */
    boolean waitForAnyToComplete(List<Future<?>> futures, long timeout, TimeUnit unit);

    /**
     * Wait for all of the futures to complete (exceptionally or not).
     *
     * @param futures list of futures
     * @param timeout timeout amount
     * @param unit    timeout unit
     * @return true if all of the futures complete within the timeout
     */
    boolean waitForAllToComplete(List<Future<?>> futures, long timeout, TimeUnit unit);
}

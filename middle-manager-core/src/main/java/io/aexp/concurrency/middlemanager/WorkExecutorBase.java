package io.aexp.concurrency.middlemanager;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.concurrent.ThreadSafe;

import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

@ThreadSafe
public abstract class WorkExecutorBase implements WorkExecutor {

    private static final long MAX_TIME_SLICE_NS = NANOSECONDS.convert(100, MILLISECONDS);

    @Override
    public boolean waitForCompletion(Future<?> f, long timeout, TimeUnit unit) {
        try {
            f.get(timeout, unit);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            return true;
        } catch (TimeoutException e) {
            return false;
        } catch (CancellationException e) {
            // I think it's defensible to say canceled => did not complete.
            return false;
        }
    }

    @Override
    public boolean waitForAnyToComplete(List<Future<?>> futures, long timeout, TimeUnit unit) {
        long start = nanoTime();
        long timeoutNanos = unit.toNanos(timeout);

        for (Future<?> future : futures) {
            long delta = timeoutNanos - (nanoTime() - start);
            if (delta <= 0) {
                return false;
            }

            if (future.isDone() || future.isCancelled()) {
                return true;
            }

            try {
                // divide remaining time into per-future slices
                long sliceOfRemainingTime = delta / futures.size();
                // cap at 100ms
                long perFutureTimeout = min(sliceOfRemainingTime, MAX_TIME_SLICE_NS);

                future.get(perFutureTimeout, NANOSECONDS);
                // we got something before the timeout
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (ExecutionException e) {
                return true;
            } catch (TimeoutException e) {
                // keep going
            } catch (CancellationException e) {
                // We don't use Future#cancel anyway, but I think it's defensible to say canceled => did not complete.
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean waitForAllToComplete(List<Future<?>> futures, long timeout, TimeUnit unit) {
        long start = nanoTime();
        long timeoutNanos = unit.toNanos(timeout);

        for (Future<?> future : futures) {
            long delta = timeoutNanos - (nanoTime() - start);
            if (delta <= 0) {
                return false;
            }

            if (future.isDone() || future.isCancelled()) {
                continue;
            }

            try {
                future.get(delta, NANOSECONDS);
                // keep going
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } catch (ExecutionException e) {
                // keep going
            } catch (TimeoutException e) {
                return false;
            } catch (CancellationException e) {
                // We don't use Future#cancel anyway, but I think it's defensible to say canceled => did not complete.
                return false;
            }
        }

        return true;
    }
}

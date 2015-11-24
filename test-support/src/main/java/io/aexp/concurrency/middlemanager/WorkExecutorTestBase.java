package io.aexp.concurrency.middlemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class WorkExecutorTestBase {

    WorkExecutor workExecutor = getWorkExecutor();

    protected abstract WorkExecutor getWorkExecutor();

    @Test
    public void testSubmitRunnableRuns() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        workExecutor.submit(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        });

        latch.await(100, MILLISECONDS);
    }

    @Test
    public void testSuccessfulRunnableEmitsRunnableOnCompletion() throws ExecutionException, InterruptedException {
        StubRunnable r = new StubRunnable();
        Future<StubRunnable> f = workExecutor.submit(r);

        assertSame(r, f.get());
    }

    @Test
    public void testSubmitCallableRuns() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        workExecutor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                latch.countDown();
                return null;
            }
        });

        latch.await(100, MILLISECONDS);
    }

    @Test
    public void testWaitForCompletionTrueForCompletesNormally() {
        Future<?> f = getSleepingFuture(100);

        assertTrue(workExecutor.waitForCompletion(f, 200, MILLISECONDS));
    }

    @Test
    public void testWaitForCompletionTrueForThrows() {
        Future<?> f = getFailedFuture();

        assertTrue(workExecutor.waitForCompletion(f, 200, MILLISECONDS));
    }

    @Test
    public void testWaitForCompletionFalseForTimeout() {
        Future<?> f = getSleepingFuture(1000);

        assertFalse(workExecutor.waitForCompletion(f, 100, MILLISECONDS));
    }

    @Test
    public void testWaitForCompletionFalseForInterruptedOrCancelled() {
        Future<?> f = getSleepingFuture(10000);

        // race to cancel before thread starts
        f.cancel(true);

        // much shorter than actual timeout, may hit CancellationException case
        assertFalse(workExecutor.waitForCompletion(f, 100, MILLISECONDS));
    }

    @Test
    public void testWaitForCompletionFalseForInterrupted() throws InterruptedException {
        Future<?> f = getInterruptedFuture();

        // much shorter than actual timeout, may hit CancellationException case
        assertFalse(workExecutor.waitForCompletion(f, 100, MILLISECONDS));
    }

    @Test
    public void testFutureThrowsExecutionExceptionOnFailure() throws InterruptedException {
        Future<?> f = getFailedFuture();

        try {
            f.get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("kaboom", e.getCause().getMessage());
        }
    }

    @Test
    public void testFutureReturnsResultForCallable() throws ExecutionException, InterruptedException {
        Future<String> f = workExecutor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "ok";
            }
        });

        assertEquals("ok", f.get());
    }

    @Test
    public void testWaitForAllFalseWhenOneCompletes() {
        Future<?> slow = getSleepingFuture(10000);
        Future<?> fast = getSleepingFuture(100);

        assertFalse(workExecutor.waitForAllToComplete(list(slow, fast), 200, MILLISECONDS));
    }

    @Test
    public void testWaitForAllTrueWhenAllComplete() {
        Future<?> f1 = getNoOpFuture();
        Future<?> f2 = getNoOpFuture();

        assertTrue(workExecutor.waitForAllToComplete(list(f1, f2), 100, MILLISECONDS));
    }

    @Test
    public void testWaitForAllFalseWhenOnePossiblyCancelled() {
        Future<?> toCancel = getSleepingFuture(10000);

        // race to cancel before thread starts
        toCancel.cancel(true);

        Future<?> slow = getSleepingFuture(10000);

        assertFalse(workExecutor.waitForAllToComplete(list(slow, toCancel), 200, MILLISECONDS));
    }

    @Test
    public void testWaitForAllFalseWhenOneInterrupted() throws InterruptedException {
        Future<?> slow = getSleepingFuture(10000);
        Future<?> interrupted = getInterruptedFuture();
        assertFalse(workExecutor.waitForAllToComplete(list(slow, interrupted), 200, MILLISECONDS));
    }

    @Test
    public void testWaitForAllTrueWhenOneFails() {
        Future<?> f1 = getNoOpFuture();
        Future<?> f2 = getFailedFuture();

        assertTrue(workExecutor.waitForAllToComplete(list(f1, f2), 100, MILLISECONDS));
    }

    @Test
    public void testWaitForAnyTrueWhenOneCompletes() {
        Future<?> slow = getSleepingFuture(10000);
        Future<?> fast = getSleepingFuture(100);

        assertTrue(workExecutor.waitForAnyToComplete(list(slow, fast), 200, MILLISECONDS));
    }

    @Test
    public void testWaitForAnyTrueWhenAllComplete() {
        Future<?> f1 = getNoOpFuture();
        Future<?> f2 = getNoOpFuture();

        assertTrue(workExecutor.waitForAnyToComplete(list(f1, f2), 100, MILLISECONDS));
    }

    @Test
    public void testWaitForAnyTrueWhenOnePossiblyCancelled() {
        Future<?> toCancel = getSleepingFuture(10000);

        // race to cancel before thread starts
        toCancel.cancel(true);

        Future<?> slow = getSleepingFuture(100);

        assertTrue(workExecutor.waitForAnyToComplete(list(slow, toCancel), 200, MILLISECONDS));
    }

    @Test
    public void testWaitForAnyTrueWhenOneInterrupted() throws InterruptedException {
        Future<?> slow = getSleepingFuture(10000);
        Future<?> interrupted = getInterruptedFuture();
        assertTrue(workExecutor.waitForAnyToComplete(list(slow, interrupted), 200, MILLISECONDS));
    }

    @Test
    public void testWaitForAnyTrueWhenOneFails() {
        Future<?> f1 = getSleepingFuture(10000);
        Future<?> f2 = getFailedFuture();

        assertTrue(workExecutor.waitForAnyToComplete(list(f1, f2), 100, MILLISECONDS));
    }

    @Test
    public void testWaitForAnyFalseWhenNoneComplete() {
        Future<?> slow = getSleepingFuture(10000);
        Future<?> slow2 = getSleepingFuture(10000);

        assertFalse(workExecutor.waitForAnyToComplete(list(slow, slow2), 100, MILLISECONDS));
    }

    private Future<?> getNoOpFuture() {
        return workExecutor.submit(new StubRunnable());
    }

    private List<Future<?>> list(Future<?>... ts) {
        List<Future<?>> list = new ArrayList<Future<?>>();
        Collections.addAll(list, ts);

        return list;
    }

    private Future<?> getSleepingFuture(final int millis) {
        return workExecutor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Thread.sleep(millis);
                return null;
            }
        });
    }

    private Future<?> getFailedFuture() {
        return workExecutor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                throw new RuntimeException("kaboom");
            }
        });
    }

    private Future<?> getInterruptedFuture() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        Future<Void> f = workExecutor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                latch.countDown();
                Thread.sleep(10000);
                return null;
            }
        });

        latch.await();

        // now we know thread is running
        f.cancel(true);
        return f;
    }

    private static class StubRunnable implements Runnable {
        @Override
        public void run() {

        }
    }
}

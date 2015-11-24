package io.aexp.concurrency.middlemanager.ibm;

import com.ibm.websphere.asynchbeans.Work;
import com.ibm.websphere.asynchbeans.WorkException;
import com.ibm.websphere.asynchbeans.WorkManager;
import io.aexp.concurrency.middlemanager.WorkExecutorBase;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class WorkManagerWorkExecutor extends WorkExecutorBase {
    private final WorkManager workManager;

    public WorkManagerWorkExecutor(WorkManager workManager) {
        this.workManager = workManager;
    }

    @Nonnull
    @Override
    public <T extends Runnable> Future<T> submit(T runnable) {
        FutureTask<T> future = new FutureTask<T>(runnable, runnable);
        run(future);
        return future;
    }

    @Nonnull
    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> future = new FutureTask<T>(callable);
        run(future);
        return future;
    }

    private void run(final Runnable runnable) {
        try {
            workManager.startWork(new WorkWrapper(runnable));
        } catch (WorkException e) {
            throw new RuntimeException("Could not schedule Work", e);
        }
    }

    private static class WorkWrapper implements Work {
        private final Runnable runnable;

        WorkWrapper(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void release() {
            // no op
        }

        @Override
        public void run() {
            runnable.run();
        }
    }
}

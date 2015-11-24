package io.aexp.concurrency.middlemanager;

import java.util.concurrent.Executors;

public final class ExecutorServiceWorkExecutorTest extends WorkExecutorTestBase {

    @Override
    protected WorkExecutor getWorkExecutor() {
        return new ExecutorServiceWorkExecutor(Executors.newCachedThreadPool());
    }
}

package io.aexp.concurrency.middlemanager.ibm;

import com.ibm.websphere.asynchbeans.AsynchScope;
import com.ibm.websphere.asynchbeans.EventSource;
import com.ibm.websphere.asynchbeans.Work;
import com.ibm.websphere.asynchbeans.WorkException;
import com.ibm.websphere.asynchbeans.WorkItem;
import com.ibm.websphere.asynchbeans.WorkListener;
import com.ibm.websphere.asynchbeans.WorkManager;
import com.ibm.websphere.asynchbeans.WorkWithExecutionContext;
import io.aexp.concurrency.middlemanager.WorkExecutor;
import io.aexp.concurrency.middlemanager.WorkExecutorTestBase;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class WorkManagerWorkExecutorTest extends WorkExecutorTestBase {

    @Override
    protected WorkExecutor getWorkExecutor() {
        return new WorkManagerWorkExecutor(new WorkManager() {

            private final ExecutorService executorService = Executors.newCachedThreadPool();

            @Override
            public WorkItem startWork(WorkWithExecutionContext workWithExecutionContext) throws WorkException {
                throw new UnsupportedOperationException();
            }

            @Override
            public WorkItem startWork(WorkWithExecutionContext workWithExecutionContext, boolean b) throws
                    WorkException {
                throw new UnsupportedOperationException();
            }

            @Override
            public WorkItem startWork(WorkWithExecutionContext workWithExecutionContext, long l,
                    WorkListener workListener) throws WorkException, IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            @Override
            public WorkItem startWork(WorkWithExecutionContext workWithExecutionContext, long l,
                    WorkListener workListener,
                    boolean b) throws WorkException, IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            @Override
            public WorkItem startWork(Work work) throws WorkException, IllegalArgumentException {
                // Enough of an impl to limp along; we don't actually use the WorkItem
                executorService.submit(work);
                return null;
            }

            @Override
            public WorkItem startWork(Work work, boolean b) throws WorkException, IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            @Override
            public WorkItem startWork(Work work, long l, WorkListener workListener) throws WorkException,
                    IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            @Override
            public WorkItem startWork(Work work, long l, WorkListener workListener, boolean b) throws WorkException,
                    IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void doWork(WorkWithExecutionContext workWithExecutionContext) throws WorkException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void doWork(WorkWithExecutionContext workWithExecutionContext, WorkListener workListener) throws
                    WorkException, IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void doWork(Work work) throws WorkException, IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void doWork(Work work, WorkListener workListener) throws WorkException, IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean join(ArrayList arrayList, boolean b, int i) {
                throw new UnsupportedOperationException();
            }

            @Override
            public WorkWithExecutionContext create(Work work) throws IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            @Override
            public EventSource createEventSource() {
                throw new UnsupportedOperationException();
            }

            @Override
            public AsynchScope createAsynchScope(String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public AsynchScope findAsynchScope(String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public AsynchScope findOrCreateAsynchScope(String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addListener(Object o) throws IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addListener(Object o, int i) throws IllegalArgumentException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeListener(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object getEventTrigger(Class aClass) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object getEventTrigger(Class aClass, boolean b) {
                throw new UnsupportedOperationException();
            }
        });
    }
}

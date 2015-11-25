[![Build Status](https://semaphoreci.com/api/v1/projects/4ec7138f-7652-45d2-9b09-a46ed96dfb04/615371/badge.svg)](https://semaphoreci.com/americanexpress/middle-manager)

# What is Middle Manager?

Like any good manager, it insulates you from unpleasant things. The goal is to help you migrate off of the `com.ibm.websphere.asynchbeans.WorkManager` API. It provides a **`WorkExecutor`** interface that's similar to [IBM's `WorkManager`](http://www-01.ibm.com/support/knowledgecenter/SSEQTP_7.0.0/com.ibm.websphere.javadoc.doc/web/apidocs/com/ibm/websphere/asynchbeans/WorkManager.html), but uses the niceties of `Executor` and friends like `Future`. The API is purposely similar, so porting should be straightforward.
 
We provide implementations of `WorkExecutor` that are backed by `WorkManager` or by `java.util.concurrent.ExecutorService`. This provides an easy path to removing the use of `WorkManager`: first target `WorkExecutor`'s API and keep using the same `WorkManager` under the hood. Then, switch to using the `ExecutorService`-backed implementation when you're ready to completely sever any dependency on `WorkManager`.

It's compatible with Java 6 because if you're using `WorkManager`, you're probably using a pretty old system!

# How do I use it?

At a high level:

- Add dependencies on `io.aexp.concurrency.middle-manager:middle-manager-core` and `io.aexp.concurrency.middle-manager:middle-manager-ibm`.
- Migrate implementations of `Work` to implement only `Runnable`. `Work` extends `Runnable`, so you can leave `Work` there for now if you want to use the same `Work` implementations in both old `WorkManager` code and new `WorkExecutor` code. 
- Wrap `WorkManager` instances with a `WorkManagerWorkExecutor`. Instead of casting `WorkItem#getResult()` to the original `Work` submitted to `WorkManager#doWork()`, you'll get a `Future<T>` where `T` is your `Runnable` implementation.
- Remove any remaining `implements Work` -- simplify it to just `implements Runnable`.
- Migrate usage of `WorkManagerWorkExecutor` to use `ExecutorWorkExecutor` instead.
- Remove all code that created or otherwise accessed `WorkManager` instances since they're no longer used to power the `WorkExecutor`s

When you no longer need the IBM implementation, remove the `middle-manager-ibm` dependency, as well as whatever dependency you were using to access `WorkManager`.

Integration will depend on how you're currently accessing your `WorkManager` objects. We, for instance, use logic like the below snippet to determine which `WorkExecutor` to use, but it's likely to be custom to your situation.

```java
private static boolean shouldUseExecutorImpl() {
    // allow forcing either way via system property
    String type = System.getProperty("com.foo.bar.baz");
    if ("workmanager".equals(type)) {
        return false;
    }
    if ("executor".equals(type)) {
        return true;
    }

    // true if you appear to be on a grown-up computer; will be false on legacy deployment. This is handy for running tests, etc.
    String arch = System.getProperty("os.arch");
    return System.getProperty("java.version").startsWith("1.8.")
            && (arch.equals("amd64") || arch.equals("x86_64"));
}
```

Once you've figured out how you wish to handle getting an `WorkExecutor` instead of a `WorkManager` (e.g. you could load a `WorkManager` from JNDI or however you're doing that, but then wrap that in a `WorkManagerWorkExecutor`), you'll want to migrate code from looking like this:

```java
WorkItem item = workManager.startWork(new FooTask());

// could also assemble an ArrayList and call WorkManager#join()
while (item.getStatus() != WorkEvent.WORK_COMPLETED) {
    Thread.sleep(1234);
}

// Only provides the ability to return the original Work as a means of expression completion
FooTask task = (FooTask) item.getResult();

// This usually means that the Work implementation writes to its own field, or something like that
String result = task.fieldThatWasWrittenToDuringExecution;
```

to this:

```java
Future<FooTask> future = workExecutor.submit(new FooTask());

// no need to change the structure of FooTask and how it records its result
String result = future.get().fieldThatWasWrittenToDuringExecution;
```

Once you're that far, it's a small leap to making `FooTask` implement `Callable<String>`, and then you can get a `Future<String>` directly.

# What is a `WorkManager`?

`WorkManager`, `WorkItem`, and friends form an old concurrency API from the early days of Java. It was originally intended to provide concurrency tools to webapps running inside an app server like WebSphere. There are actually several flavors of it: [CommonJ](https://docs.oracle.com/cd/E13222_01/wls/docs92/commonj/commonj.html), [IBM](http://www-01.ibm.com/support/knowledgecenter/SSEQTP_7.0.0/com.ibm.websphere.javadoc.doc/web/apidocs/com/ibm/websphere/asynchbeans/WorkManager.html), and even an [attempt at standardization in Java EE](https://docs.oracle.com/javaee/5/api/javax/resource/spi/work/WorkManager.html). All three are similar, and while this project includes an implementation that can use an IBM `WorkManager` as the actual thread handling mechanism, it would be straightforward to also implement this on top of the CommonJ or Java EE 5 flavors. Since all three APIs are similar, this library is useful for migrating away from all three.

# Building

If you want to build everything except the IBM subproject, add the `middleManager.excludeIbmSubproject` Gradle property to your build, as in:

```
./gradlew build -PmiddleManager.excludeIbmSubproject
```

Unfortunately, there isn't a publicly available source for the `com.ibm.websphere.asynchbeans.*` classes, so this project's IBM subproject will not build without some manual setup. In your WebSphere installation, there is probably an `asynchbeans.jar` file somewhere that has those classes in it. Drop that jar in the `middle-manager-ibm/ext-jars` dir within this project and gradle will find the classes it needs to build middle-manager. If you have a `runtime` jar, that may also contain the `WorkManager` classes.

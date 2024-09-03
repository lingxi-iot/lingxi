package cn.gofree.lingxi.eventcenter.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadUtils {
    private static final Logger LOGGER =  LoggerFactory.getLogger(ThreadUtils.class);

    public static ExecutorService newThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, String processName, boolean isDaemon) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, newThreadFactory(processName, isDaemon));
    }

    public static ExecutorService newSingleThreadExecutor(String processName, boolean isDaemon) {
        return Executors.newSingleThreadExecutor(newThreadFactory(processName, isDaemon));
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String processName, boolean isDaemon) {
        return Executors.newSingleThreadScheduledExecutor(newThreadFactory(processName, isDaemon));
    }

    public static ScheduledExecutorService newFixedThreadScheduledPool(int nThreads, String processName, boolean isDaemon) {
        return Executors.newScheduledThreadPool(nThreads, newThreadFactory(processName, isDaemon));
    }

    public static ThreadFactory newThreadFactory(String processName, boolean isDaemon) {
        return newGenericThreadFactory("Remoting-" + processName, isDaemon);
    }

    public static ThreadFactory newGenericThreadFactory(String processName) {
        return newGenericThreadFactory(processName, false);
    }

    public static ThreadFactory newGenericThreadFactory(String processName, int threads) {
        return newGenericThreadFactory(processName, threads, false);
    }

    public static ThreadFactory newGenericThreadFactory(final String processName, final boolean isDaemon) {
        return new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, String.format("%s_%d", processName, this.threadIndex.incrementAndGet()));
                thread.setDaemon(isDaemon);
                return thread;
            }
        };
    }

    public static ThreadFactory newGenericThreadFactory(final String processName, final int threads, final boolean isDaemon) {
        return new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, String.format("%s_%d_%d", processName, threads, this.threadIndex.incrementAndGet()));
                thread.setDaemon(isDaemon);
                return thread;
            }
        };
    }

    public static Thread newThread(String name, Runnable runnable, boolean daemon) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(daemon);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                ThreadUtils.LOGGER.error("Uncaught exception in thread '" + t.getName() + "':", e);
            }
        });
        return thread;
    }

    public static void shutdownGracefully(Thread t) {
        shutdownGracefully(t, 0L);
    }

    public static void shutdownGracefully(Thread t, long millis) {
        if (t != null) {
            while(t.isAlive()) {
                try {
                    t.interrupt();
                    t.join(millis);
                } catch (InterruptedException var4) {
                    Thread.currentThread().interrupt();
                }
            }

        }
    }

    public static void shutdownGracefully(ExecutorService executor, long timeout, TimeUnit timeUnit) {
        executor.shutdown();

        try {
            if (!executor.awaitTermination(timeout, timeUnit)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(timeout, timeUnit)) {
                    LOGGER.warn(String.format("%s didn't terminate!", executor));
                }
            }
        } catch (InterruptedException var5) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }

    public static void shutdown(ExecutorService executorService) {
        if (executorService != null) {
            executorService.shutdown();
        }

    }

    private ThreadUtils() {
    }
}

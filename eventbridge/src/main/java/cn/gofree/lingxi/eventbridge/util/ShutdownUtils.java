package cn.gofree.lingxi.eventbridge.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownUtils.class);

    public static void shutdownThreadPool(ExecutorService executor) {
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(60, TimeUnit.SECONDS);
            } catch (Exception e) {
                LOGGER.error("Shutdown threadPool failed", e);
            }
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }
    }
}

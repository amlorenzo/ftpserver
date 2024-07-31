package com.irg.ftpserver.service;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class SFTPExecutorService {
    private final Logger logger = LoggerFactory.getLogger(SFTPExecutorService.class);
    private final int corePoolSize;
    private final int maximumPoolSize;
    private final long keepAliveTime;
    private final int queueCapacity;
    private final ThreadPoolExecutor threadPoolExecutor;

    public SFTPExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueCapacity) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.queueCapacity = queueCapacity;

        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<> (this.queueCapacity);
        ThreadFactory threadFactory = new CustomThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

        this.threadPoolExecutor = new ThreadPoolExecutor(
                this.corePoolSize,
                this.maximumPoolSize,
                this.keepAliveTime,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                handler
        );
    }

    public void shutdown() {
        try{
            logger.info("Shutting down the SFTP Executor service: {}", this.threadPoolExecutor);
            this.threadPoolExecutor.shutdown();
            if (!this.threadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.info("Shutting down the SFTP Executor service forcefully: {}",
                            this.threadPoolExecutor);
                this.threadPoolExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Error while shutting down the SFTP Executor service", e);
            this.threadPoolExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static class CustomThreadFactory implements ThreadFactory {
        private static final Logger logger = LoggerFactory.getLogger(CustomThreadFactory.class);
        private static final String THREAD_NAME_PREFIX = "SFTP-Executor-Service-Thread-";
        private static final AtomicInteger threadCount = new AtomicInteger(0);

        @NotNull
        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(THREAD_NAME_PREFIX + threadCount.getAndIncrement());
            logger.debug("Created new thread: {}", thread.getName());
            return thread;
        }
    }
}

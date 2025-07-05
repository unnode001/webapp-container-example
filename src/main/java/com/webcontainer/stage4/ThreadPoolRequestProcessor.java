package com.webcontainer.stage4;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 第四阶段：线程池请求处理器
 * 使用固定大小线程池替代"一连接一线程"模型
 */
public class ThreadPoolRequestProcessor {
    private final ThreadPoolExecutor threadPool;
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final long startTime = System.currentTimeMillis();

    public ThreadPoolRequestProcessor(int corePoolSize, int maximumPoolSize, int queueCapacity) {
        this.threadPool = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new WebServerThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        System.out.println("线程池初始化完成:");
        System.out.println("  核心线程数: " + corePoolSize);
        System.out.println("  最大线程数: " + maximumPoolSize);
        System.out.println("  队列容量: " + queueCapacity);
    }

    /**
     * 提交请求处理任务
     */
    public void submit(Runnable task) {
        try {
            threadPool.execute(() -> {
                int requestId = requestCounter.incrementAndGet();
                String threadName = Thread.currentThread().getName();

                long startTime = System.currentTimeMillis();
                try {
                    System.out.println("[" + threadName + "] 开始处理请求 #" + requestId);
                    task.run();
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    System.out.println("[" + threadName + "] 完成请求 #" + requestId + "，耗时: " + duration + "ms");
                }
            });
        } catch (Exception e) {
            System.err.println("提交请求处理任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取线程池统计信息
     */
    public ThreadPoolStats getStats() {
        return new ThreadPoolStats(
                threadPool.getCorePoolSize(),
                threadPool.getMaximumPoolSize(),
                threadPool.getActiveCount(),
                threadPool.getPoolSize(),
                threadPool.getQueue().size(),
                threadPool.getQueue().remainingCapacity(),
                threadPool.getCompletedTaskCount(),
                requestCounter.get(),
                System.currentTimeMillis() - startTime);
    }

    /**
     * 优雅关闭线程池
     */
    public void shutdown() {
        System.out.println("开始关闭线程池...");

        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println("强制关闭线程池...");
                threadPool.shutdownNow();

                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("线程池关闭失败");
                }
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("线程池已关闭，共处理了 " + requestCounter.get() + " 个请求");
    }

    /**
     * 自定义线程工厂
     */
    private static class WebServerThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix = "WebServer-Worker-";

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    /**
     * 线程池统计信息
     */
    public static class ThreadPoolStats {
        private final int corePoolSize;
        private final int maximumPoolSize;
        private final int activeCount;
        private final int poolSize;
        private final int queueSize;
        private final int remainingCapacity;
        private final long completedTaskCount;
        private final int totalRequestCount;
        private final long uptimeMillis;

        public ThreadPoolStats(int corePoolSize, int maximumPoolSize, int activeCount,
                int poolSize, int queueSize, int remainingCapacity,
                long completedTaskCount, int totalRequestCount, long uptimeMillis) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.activeCount = activeCount;
            this.poolSize = poolSize;
            this.queueSize = queueSize;
            this.remainingCapacity = remainingCapacity;
            this.completedTaskCount = completedTaskCount;
            this.totalRequestCount = totalRequestCount;
            this.uptimeMillis = uptimeMillis;
        }

        // Getters
        public int getCorePoolSize() {
            return corePoolSize;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public int getActiveCount() {
            return activeCount;
        }

        public int getPoolSize() {
            return poolSize;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public int getRemainingCapacity() {
            return remainingCapacity;
        }

        public long getCompletedTaskCount() {
            return completedTaskCount;
        }

        public int getTotalRequestCount() {
            return totalRequestCount;
        }

        public long getUptimeMillis() {
            return uptimeMillis;
        }

        public String getUptimeFormatted() {
            long seconds = uptimeMillis / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;

            return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
        }

        @Override
        public String toString() {
            return String.format(
                    "ThreadPool{core=%d, max=%d, active=%d, pool=%d, queue=%d/%d, completed=%d, total=%d, uptime=%s}",
                    corePoolSize, maximumPoolSize, activeCount, poolSize,
                    queueSize, queueSize + remainingCapacity, completedTaskCount,
                    totalRequestCount, getUptimeFormatted());
        }
    }
}

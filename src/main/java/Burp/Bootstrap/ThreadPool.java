package Burp.Bootstrap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
    private static volatile ExecutorService threadPool;
    private static volatile int currentPoolSize;

    private ThreadPool() {
    }

    /**
     * 获取线程池
     *
     * @param poolSize 线程池大小
     * @return 线程池
     */
    public static ExecutorService getThreadPool(int poolSize) {
        if (threadPool == null || currentPoolSize != poolSize) {
            synchronized (ThreadPool.class) {
                if (threadPool == null || currentPoolSize != poolSize) {
                    if (threadPool != null) {
                        clearThreadPool();
                    }
                    threadPool = Executors.newFixedThreadPool(poolSize);
                    currentPoolSize = poolSize;
                }
            }
        }
        return threadPool;
    }

    /**
     * 清理线程池
     */
    public static void clearThreadPool() {
        if (threadPool != null) {
            threadPool.shutdownNow();
            threadPool = null;
            currentPoolSize = 0;
        }
    }
}
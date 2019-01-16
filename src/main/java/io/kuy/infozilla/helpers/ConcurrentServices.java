/**
 * ConcurrentServices.java
 * This file is part of the infoZilla framework and tool.
 */
package io.kuy.infozilla.helpers;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class ConcurrentServices {

	private static ExecutorService executorService = Executors.newCachedThreadPool();
    

    public static <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }
    
    public static Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    public static <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(task, result);
    }
    
    public static void shutdown() {
        executorService.shutdown();
    }

}
package org.abr.memearenabot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous tasks
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);
    
    @Value("${async.core-pool-size:5}")
    private int corePoolSize;
    
    @Value("${async.max-pool-size:10}")
    private int maxPoolSize;
    
    @Value("${async.queue-capacity:25}")
    private int queueCapacity;
    
    @Value("${async.thread-name-prefix:meme-async-}")
    private String threadNamePrefix;
    
    /**
     * Configure async executor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
    
    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
    
    /**
     * Custom exception handler for async methods
     */
    private static class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            logger.error("Async exception occurred in method: {}", method.getName(), ex);
            
            // Log parameters if needed
            if (params != null && params.length > 0) {
                StringBuilder paramInfo = new StringBuilder();
                for (int i = 0; i < params.length; i++) {
                    paramInfo.append("Parameter value ").append(i).append(": ");
                    if (params[i] == null) {
                        paramInfo.append("null");
                    } else {
                        paramInfo.append(params[i].toString());
                    }
                    if (i < params.length - 1) {
                        paramInfo.append(", ");
                    }
                }
                logger.error("Method parameters: {}", paramInfo);
            }
        }
    }
} 
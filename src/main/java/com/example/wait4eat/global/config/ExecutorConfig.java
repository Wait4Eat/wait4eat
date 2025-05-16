package com.example.wait4eat.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ExecutorConfig {

    @Bean(name = "outboxExecutor")
    public ExecutorService outboxExecutor() {
        AtomicInteger threadNumber = new AtomicInteger(1);

        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("outbox-executor-" + threadNumber.getAndIncrement());
            return thread;
        };

        return new ThreadPoolExecutor(
          10,
          20,
          60L, TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(1000),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}




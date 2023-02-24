package com.example.demo.service;

import com.example.demo.model.ThreadResult;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

@Service
@NoArgsConstructor
public class ThreadServiceRunInformation {
    private Map<String, ThreadWrapper> threads = new ConcurrentHashMap<>();

    public CompletableFuture<ThreadResult> createThread(String name, Supplier<CompletableFuture<ThreadResult>> supplier) {
        ThreadWrapper threadWrapper = new ThreadWrapper(supplier);
        Thread thread = new Thread(threadWrapper, name);
        threads.put(name, threadWrapper);
        thread.start();
        return threadWrapper.getFuture();
    }

    public void stopThread(String name) {
        ThreadWrapper threadWrapper = threads.get(name);
        if (threadWrapper != null) {
            threadWrapper.stop();
            threads.remove(name);
        }
    }

    private static class ThreadWrapper implements Runnable {

        private final Supplier<CompletableFuture<ThreadResult>> supplier;
        private CompletableFuture<ThreadResult> future;
        private volatile boolean stopped;

        public ThreadWrapper(Supplier<CompletableFuture<ThreadResult>> supplier) {
            this.supplier = supplier;
            this.future = new CompletableFuture<>();
        }

        @Override
        public void run() {
            CompletableFuture<ThreadResult> computation = supplier.get();
            computation.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    future.completeExceptionally(throwable);
                } else {
                    future.complete(result);
                }
            });
            try {
                computation.get();
            } catch (InterruptedException | ExecutionException e) {
                future.completeExceptionally(e);
            }
        }

        public synchronized void stop() {
            stopped = true;
            future.completeExceptionally(new ThreadStoppedException());
        }

        public synchronized boolean isStopped() {
            return stopped;
        }

        public CompletableFuture<ThreadResult> getFuture() {
            return future;
        }
    }

    private static class ThreadStoppedException extends RuntimeException {
        // empty exception class to signal that the thread was stopped
    }
}

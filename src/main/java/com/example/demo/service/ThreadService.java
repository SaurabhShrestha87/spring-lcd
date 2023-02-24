package com.example.demo.service;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@NoArgsConstructor
public class ThreadService {

    private final Map<String, ThreadWrapper> threads = new ConcurrentHashMap<>();

    public void createThread(String name, Runnable runnable) {
        ThreadWrapper threadWrapper = new ThreadWrapper(runnable);
        Thread thread = new Thread(threadWrapper, name);
        threads.put(name, threadWrapper);
        thread.start();
    }

    public void pauseThread(String name) {
        ThreadWrapper threadWrapper = threads.get(name);
        if (threadWrapper != null) {
            threadWrapper.pause();
        }
    }

    public void resumeThread(String name) {
        ThreadWrapper threadWrapper = threads.get(name);
        if (threadWrapper != null) {
            threadWrapper.resume();
        }
    }

    public void stopThread(String name) {
        ThreadWrapper threadWrapper = threads.get(name);
        if (threadWrapper != null) {
            threadWrapper.stop();
            threads.remove(name);
        }
    }

    public void restartThread(String name) {
        ThreadWrapper threadWrapper = threads.get(name);
        if (threadWrapper != null) {
            threadWrapper.stop();
            threads.remove(name);
            ThreadWrapper newThreadWrapper = new ThreadWrapper(threadWrapper.getRunnable());
            Thread newThread = new Thread(newThreadWrapper, name);
            threads.put(name, newThreadWrapper);
            newThread.start();
        }
    }

    private static class ThreadWrapper implements Runnable {

        private final Runnable runnable;
        private volatile boolean paused;
        private volatile boolean stopped;

        public ThreadWrapper(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            while (!stopped) {
                if (!paused) {
                    runnable.run();
                }
            }
        }

        public synchronized void pause() {
            paused = true;
        }

        public synchronized void resume() {
            paused = false;
            notifyAll();
        }

        public synchronized void stop() {
            stopped = true;
            notifyAll();
        }

        public synchronized boolean isPaused() {
            return paused;
        }

        public synchronized boolean isStopped() {
            return stopped;
        }

        public synchronized void waitIfPaused() throws InterruptedException {
            while (paused && !stopped) {
                wait();
            }
        }

        public Runnable getRunnable() {
            return runnable;
        }
    }
}
package com.pcm.alert.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 异步推送器 —— 通过内存队列解耦告警生产与推送。
 * <p>
 * 内部使用单线程从有界队列中消费消息并委托给底层推送器。
 * 队列满时降级为同步直接推送，避免消息丢失。
 * </p>
 */
public class AsyncAlertPublisher implements AlertPublisher {
    private static final Logger log = LoggerFactory.getLogger(AsyncAlertPublisher.class);

    private final AlertPublisher delegate;
    private final BlockingQueue<AlertMessage> queue;
    private final ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public AsyncAlertPublisher(AlertPublisher delegate, int queueSize) {
        this.delegate = delegate;
        this.queue = new ArrayBlockingQueue<>(Math.max(queueSize, 1));
        this.executorService = Executors.newSingleThreadExecutor(new AlertThreadFactory());
        this.executorService.submit(this::drain);
    }

    @Override
    public void publish(AlertMessage message) {
        if (!queue.offer(message)) {
            log.warn("Alert queue is full. fallback to direct publish. title={}", message.getTitle());
            delegate.publish(message);
        }
    }

    /**
     * 优雅关闭：停止接收新消息，等待队列清空后退出。
     */
    public void shutdown() {
        running.set(false);
        executorService.shutdownNow();
    }

    private void drain() {
        while (running.get() || !queue.isEmpty()) {
            try {
                AlertMessage message = queue.poll(1, TimeUnit.SECONDS);
                if (message != null) {
                    delegate.publish(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.warn("Publish alert message failed. {}", e.getMessage(), e);
            }
        }
    }

    private static class AlertThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "pcm-alert-publisher");
            thread.setDaemon(true);
            return thread;
        }
    }
}

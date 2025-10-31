package com.example.myapplication.map;

import android.os.Handler;
import android.os.Looper;
import com.example.myapplication.util.LogUtil;

/**
 * Centralizes debounce and throttle logic for map-related operations.
 */
public class MapSchedulers {

    private static final String TAG = "MapSchedulers";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingRunnable = null;
    private long lastExecutionTime = 0;

    private final long debounceDelayMs;
    private final long throttleIntervalMs;

    public MapSchedulers(long debounceDelayMs, long throttleIntervalMs) {
        this.debounceDelayMs = debounceDelayMs;
        this.throttleIntervalMs = throttleIntervalMs;
    }

    /**
     * Schedule a task with debounce and throttle.
     * If called too soon (throttle), the call is ignored.
     * Otherwise, any pending task is canceled and a new one is scheduled after debounce delay.
     */
    public void schedule(Runnable task) {
        long currentTime = System.currentTimeMillis();

        // Throttle: ignore if called too soon
        if (currentTime - lastExecutionTime < throttleIntervalMs) {
            LogUtil.d(TAG, "Throttled: too soon since last execution");
            return;
        }

        // Cancel any pending debounced task
        if (pendingRunnable != null) {
            handler.removeCallbacks(pendingRunnable);
            pendingRunnable = null;
        }

        // Schedule new task with debounce
        pendingRunnable = new Runnable() {
            @Override
            public void run() {
                lastExecutionTime = System.currentTimeMillis();
                task.run();
                pendingRunnable = null;
            }
        };
        handler.postDelayed(pendingRunnable, debounceDelayMs);
        LogUtil.d(TAG, "Debounce timer started, will execute in " + debounceDelayMs + "ms");
    }

    /**
     * Cancel any pending scheduled task.
     */
    public void cancel() {
        if (pendingRunnable != null) {
            handler.removeCallbacks(pendingRunnable);
            pendingRunnable = null;
        }
    }

    /**
     * Clean up resources.
     */
    public void destroy() {
        cancel();
    }
}


package com.echain.stockwinner.logic;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncExecutor {
    public interface Callback {
        void onComplete();
    }

    private static ExecutorService sExecutor = Executors.newCachedThreadPool();

    public static void execute(final ExecutableTask task, final Callback callback) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (callback != null)
                    callback.onComplete();
            }
        };

        sExecutor.submit(new Runnable() {
            @Override
            public void run() {
                task.execute();
                handler.sendEmptyMessage(0);
            }
        });
    }

    public static void stop() {
        sExecutor.shutdownNow();
    }
}

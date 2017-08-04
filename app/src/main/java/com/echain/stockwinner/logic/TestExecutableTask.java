package com.echain.stockwinner.logic;

import android.util.Log;

public class TestExecutableTask implements ExecutableTask {
    private static final String TAG = "TestExecutableTask";
    @Override
    public void execute() {
        for (int i = 0; i < 5; i++) {
            Log.d(TAG, "excute " + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

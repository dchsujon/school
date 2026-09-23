package com.chitrakote.panjabiwholesale;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class NotificationJob extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        new Thread(() -> {
            try { NotificationSync.poll(this, false); } catch (Throwable ignored) {}
            jobFinished(params, false);
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}

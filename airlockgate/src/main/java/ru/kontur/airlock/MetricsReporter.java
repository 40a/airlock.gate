package ru.kontur.airlock;

import com.codahale.metrics.Meter;
import org.rapidoid.log.Log;

import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsReporter {
    private double lastThroughput = 0;
    private long lastThroughputBytes = 0;

    private long lastRequestCount = 0;
    private long lastRequestSizeCount = 0;

    public MetricsReporter(int reportingIntervalSeconds, Meter requestMeter, Meter requestSizeMeter) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            synchronized (this) {
                long prevRequestCount = lastRequestCount;
                long prevRequestSizeCount = lastRequestSizeCount;
                lastRequestCount = requestMeter.getCount();
                lastRequestSizeCount = requestSizeMeter.getCount();
                lastThroughput = ((double) (lastRequestCount - prevRequestCount)) / reportingIntervalSeconds;
                lastThroughputBytes = (lastRequestSizeCount - prevRequestSizeCount) / reportingIntervalSeconds;
            }
            Log.info(String.format("Thr-put: %s events/sec; Thr-put: %s Kb/sec", getLastThroughput(), getLastThroughputKb()));
        }, 0, reportingIntervalSeconds, TimeUnit.SECONDS);
    }

    public String getLastThroughput() {
        return new DecimalFormat("#.##").format(lastThroughput);
    }

    public String getLastThroughputKb() {
        return new DecimalFormat("#.##").format(lastThroughputBytes / 1024 );
    }
}

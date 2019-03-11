package com.orderddos.network.decisions;

import com.orderddos.network.LoadStatistics;
import com.orderddos.network.StatisticsRecorder;
import io.netty.handler.traffic.TrafficCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

public class StatisticsRecorderImpl implements StatisticsRecorder {

    private static final Logger log = LoggerFactory.getLogger(StatisticsRecorderImpl.class);

    private final Deque<LoadStatistics> last10SecondsStat = new LinkedList<>();

    private final AtomicLong requestsSent = new AtomicLong(0);
    private final AtomicLong responsesReceived = new AtomicLong(0);
    private final AtomicLong sumRequestTime = new AtomicLong(0);
    private final TrafficCounter counter;

    public StatisticsRecorderImpl(TrafficCounter counter) {
        this.counter = counter;
    }

    @Override
    public synchronized void recordRequestSent() {
        requestsSent.incrementAndGet();
    }

    @Override
    public synchronized void recordResponseReceived(long elapsedMs, int code) {
        responsesReceived.incrementAndGet();
        sumRequestTime.addAndGet(elapsedMs);
    }

    @Override
    public synchronized Deque<LoadStatistics> lastStatistics() {
        return new LinkedList<>(last10SecondsStat);
    }

    @Override
    public synchronized void takeSnapshot(long connectionsCount) {
        double avgRequestTime;
        if (responsesReceived.get() != 0) {
            avgRequestTime = sumRequestTime.get() / responsesReceived.get();
        } else {
            avgRequestTime = 0;
        }
        LoadStatistics loadStatistics = new LoadStatistics(
                requestsSent.get(),
                responsesReceived.get(),
                avgRequestTime,
                counter.cumulativeWrittenBytes(),
                counter.cumulativeReadBytes(),
                connectionsCount);
        log.info("Statistics snapshot: {}", loadStatistics.toString());
        last10SecondsStat.add(loadStatistics);
        resetCounters();
    }

    private void resetCounters() {
        counter.resetCumulativeTime();
        responsesReceived.set(0);
        requestsSent.set(0);
        sumRequestTime.set(0);
    }
}

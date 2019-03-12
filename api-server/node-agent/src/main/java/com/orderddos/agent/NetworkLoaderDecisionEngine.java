package com.orderddos.agent;

import com.codahale.metrics.Meter;
import com.orderddos.network.DecisionEngine;
import com.orderddos.network.LoadStatistics;
import com.orderddos.network.decisions.ChangeAmountOfConnections;
import com.orderddos.network.decisions.Decision;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class NetworkLoaderDecisionEngine implements DecisionEngine {

    private static final Logger log = LoggerFactory.getLogger(NetworkLoaderDecisionEngine.class);
    private static final Long GBIT = 134217728L; // 2^27

    private final Meter bytesReadPerSecond;
    private final Meter bytesWrittenPerSecond;
    private final Meter traffic;

    // Pair of connections and traffic
    private final AtomicReference<Pair<Long, Long>> bestResult = new AtomicReference<>(null);

    private final Random random = new Random();

    public NetworkLoaderDecisionEngine(Meter bytesReadPerSecond, Meter bytesWrittenPerSecond, Meter traffic) {
        this.bytesReadPerSecond = bytesReadPerSecond;
        this.bytesWrittenPerSecond = bytesWrittenPerSecond;
        this.traffic = traffic;
    }

    @Override
    public Decision makeDecision(Deque<LoadStatistics> loadStatistics) {
        LoadStatistics lastStat = loadStatistics.pollLast();
        Objects.requireNonNull(lastStat);
        bytesReadPerSecond.mark(lastStat.getBytesRead());
        updateBestResult(lastStat, bestResult);
        bytesWrittenPerSecond.mark(lastStat.getBytesWritten());
        long trafficValue = lastStat.getBytesRead() + lastStat.getBytesWritten();
        traffic.mark(trafficValue);
        Decision result;
        if (trafficValue > (GBIT * 0.75)) {
            result = new ChangeAmountOfConnections(0);
        } else {
            if (lastStat.getConnectionsCount() == 0) {
                // start with 100 connections
                result = new ChangeAmountOfConnections(100);
            } else if (trafficValue < bestResult.get().getValue1()) {
                if (Math.random() < 0.5 && lastStat.getConnectionsCount() >= 100) {
                    result = new ChangeAmountOfConnections(-50);
                } else {
                    result = new ChangeAmountOfConnections(25 + random.nextInt(25));
                }
            } else {
                // we've got new result
                result = new ChangeAmountOfConnections(0);
            }
        }
        updateBestResult(lastStat, bestResult);
        return result;
    }

    private void updateBestResult(LoadStatistics lastStat, AtomicReference<Pair<Long, Long>> bestResult) {
        long trafficValue = lastStat.getBytesRead() + lastStat.getBytesWritten();
        long connectionsCount = lastStat.getConnectionsCount();
        if (bestResult.get() == null || bestResult.get().getValue1() < trafficValue) {
            bestResult.set(new Pair<>(connectionsCount, trafficValue));
            log.info("New best traffic value: '{}'. Connections: '{}'", LoadStatistics.humanReadableByteCount(trafficValue, true), connectionsCount);
        }
    }
}

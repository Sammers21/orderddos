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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class NetworkLoaderDecisionEngine implements DecisionEngine {

    private static final Logger log = LoggerFactory.getLogger(NetworkLoaderDecisionEngine.class);
    private static final Long GBIT = 134217728L; // 2^27

    private static final int MIN_AMOUNT_OF_CONNECTIONS = 100;
    private static final int RANDOM_MAX_CONNECTION_BOOST = 25;
    private static final int MAX_CONNECTION_DOWN = -10;
    private static final int CONNECTION_BOOST_BASELINE = 25;

    private final Meter bytesReadPerSecond;
    private final Meter bytesWrittenPerSecond;
    private final Meter traffic;

    // Pair of connections and traffic
    private final AtomicReference<Pair<Long, Long>> bestResult = new AtomicReference<>(null);

    // Amount of connections to keep
    private final AtomicLong desiredAmountOfConnections = new AtomicLong(MIN_AMOUNT_OF_CONNECTIONS);

    private final Random random = new Random();

    public NetworkLoaderDecisionEngine(Meter bytesReadPerSecond, Meter bytesWrittenPerSecond, Meter traffic) {
        this.bytesReadPerSecond = bytesReadPerSecond;
        this.bytesWrittenPerSecond = bytesWrittenPerSecond;
        this.traffic = traffic;
    }

    @Override
    public Decision makeDecision(Deque<LoadStatistics> loadStatistics) {
        LoadStatistics lastStat = loadStatistics.pollLast();
        LoadStatistics preLastStat = loadStatistics.pollLast();
        Objects.requireNonNull(lastStat);
        bytesReadPerSecond.mark(lastStat.getBytesRead());
        bytesWrittenPerSecond.mark(lastStat.getBytesWritten());
        long trafficValue = lastStat.getBytesRead() + lastStat.getBytesWritten();
        traffic.mark(trafficValue);
        double trafficUtilization = (double) trafficValue / GBIT * 100;
        log.info("1 Gbit/s traffic utilization: '{}%'", String.format("%.2f", trafficUtilization));
        Decision result = null;
        int diffBetweenActualAndDesired = Math.toIntExact(desiredAmountOfConnections.get() - lastStat.getConnectionsCount());
        if (trafficValue > (GBIT * 0.75)) {
            result = new ChangeAmountOfConnections(diffBetweenActualAndDesired);
        } else if (preLastStat != null) {
            if (Math.random() < 0.5 && lastStat.getConnectionsCount() >= MIN_AMOUNT_OF_CONNECTIONS) {
                result = new ChangeAmountOfConnections(diffBetweenActualAndDesired + MAX_CONNECTION_DOWN);
            } else {
                result = new ChangeAmountOfConnections(diffBetweenActualAndDesired + CONNECTION_BOOST_BASELINE + random.nextInt(RANDOM_MAX_CONNECTION_BOOST));
            }
        } else {
            result = new ChangeAmountOfConnections(diffBetweenActualAndDesired);
        }

        updateBestResult(lastStat, lastStat, bestResult);
        return result;
    }

    private void updateBestResult(LoadStatistics statForTraffic, LoadStatistics statForConnections, AtomicReference<Pair<Long, Long>> bestResult) {
        long trafficValue = statForTraffic.getBytesRead() + statForTraffic.getBytesWritten();
        long connectionsCount = statForConnections.getConnectionsCount();
        if (bestResult.get() == null || bestResult.get().getValue1() < trafficValue) {
            bestResult.set(new Pair<>(connectionsCount, trafficValue));
            log.info("New best traffic value: '{}'. Connections: '{}'", LoadStatistics.humanReadableByteCount(trafficValue, true), connectionsCount);
        }
    }
}

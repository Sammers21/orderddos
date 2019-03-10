package com.orderddos.network;

import java.util.Queue;

/**
 * Implementation record statics and report results via {@link #lastStatistics()}
 */
public interface StatisticsRecorder {

    /**
     * Record the fact of request sending.
     */
    void recordRequestSent();

    /**
     * Record Http Request result.
     *
     * @param elapsedMs the request/response time
     * @param code the response http code
     */
    void recordResponseReceived(long elapsedMs, int code);

    /**
     * @return request/response statistics for a few last seconds
     */
    Queue<LoadStatistics> lastStatistics();

    /**
     * Force to make a statistics snapshot.
     */
    void takeSnapshot();
}

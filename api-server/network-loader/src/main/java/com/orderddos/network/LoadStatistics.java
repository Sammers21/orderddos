package com.orderddos.network;

/**
 * Represents request/response statistics for a specific amount of time.
 */
public class LoadStatistics {

    private final long requestsSent;
    private final long requestsReceived;
    private final double averageRequestTime;
    private final long bytesWritten;
    private final long bytesRead;
    private final long connectionsCount;

    public LoadStatistics(long requestsSent, long responsesReceived, double averageRequestTime, long bytesWritten, long bytesRead, long connectionsCount) {
        this.requestsSent = requestsSent;
        this.requestsReceived = responsesReceived;
        this.averageRequestTime = averageRequestTime;
        this.bytesWritten = bytesWritten;
        this.bytesRead = bytesRead;
        this.connectionsCount = connectionsCount;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public long getConnectionsCount() {
        return connectionsCount;
    }

    public long getTraffic() {
        return getBytesRead() + getBytesWritten();
    }

    public long getRequestsSent() {
        return requestsSent;
    }

    public long getRequestsReceived() {
        return requestsReceived;
    }

    public double getAverageRequestTime() {
        return averageRequestTime;
    }

    @Override
    public String toString() {
        String written = humanReadableByteCount(bytesWritten, true);
        String read = humanReadableByteCount(bytesRead, true);
        return "LoadStatistics{" +
                "requestsSent=" + requestsSent +
                ", requestsReceived=" + requestsReceived +
                ", averageRequestTime=" + averageRequestTime +
                ", bytesWritten=" + written +
                ", bytesRead=" + read +
                ", connectionsCount=" + connectionsCount +
                '}';
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}

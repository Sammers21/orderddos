package com.orderddos.network.netty;

import io.netty.channel.ChannelId;

import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ChannelsInfo {

    private final Map<String, Queue<Long>> timings = new ConcurrentHashMap<>();

    public long pollRequestSentTimeForChannel(ChannelId channelId) {
        AtomicLong reference = new AtomicLong(0);
        timings.compute(channelId.asLongText(), (channelIdString, timings) -> {
            Long poll = timings.poll();
            Objects.requireNonNull(poll);
            reference.set(poll);
            if (timings.size() == 0) {
                return null;
            } else {
                return timings;
            }
        });
        return reference.get();
    }

    public void putRequestSentTimeForChannel(ChannelId channelId) {
        long currentTimeMillis = System.currentTimeMillis();
        timings.compute(channelId.asLongText(), (channelIdString, timings) -> {
            if (timings == null) {
                Queue<Long> queue = new LinkedList<>();
                queue.add(currentTimeMillis);
                return queue;
            } else {
                timings.add(currentTimeMillis);
                return timings;
            }
        });
    }

    public void removeChannel(ChannelId channelId) {
        timings.remove(channelId);
    }

    public long inflightRequestsForChannel(ChannelId channelId) {
        Queue<Long> longs = timings.get(channelId.asLongText());
        if (longs == null || longs.size() == 0) {
            return 0;
        } else {
            return longs.size();
        }
    }
}

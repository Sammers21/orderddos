package com.orderddos.network.netty;

import com.orderddos.network.StatisticsRecorder;
import com.orderddos.network.decisions.ChangeAmountOfConnections;
import com.orderddos.network.decisions.Decision;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class NetworkController {

    private static final Logger log = LoggerFactory.getLogger(NetworkController.class);

    private final Bootstrap bootstrap;
    private final ChannelGroup channels;
    private final URI url;
    private final ChannelsInfo channelsInfo;
    private final StatisticsRecorder statisticsRecorder;
    private final Vertx vertx;

    public NetworkController(Vertx vertx, Bootstrap bootstrap, URI url, ChannelsInfo channelsInfo, StatisticsRecorder statisticsRecorder) {
        this.bootstrap = bootstrap;
        this.vertx = vertx;
        this.url = url;
        this.statisticsRecorder = statisticsRecorder;
        this.channelsInfo = channelsInfo;
        channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    public long aliveConnections() {
        return channels.size();
    }

    public void processDecision(Decision decision) {
        synchronized (channels) {
            if (decision instanceof ChangeAmountOfConnections) {
                ChangeAmountOfConnections connections = (ChangeAmountOfConnections) decision;
                changeConnections(connections);
            } else {
                throw new IllegalStateException("Unknown decision");
            }
        }
    }

    private void changeConnections(ChangeAmountOfConnections changeAmountOfConnections) {
        int change = changeAmountOfConnections.getChange();
        if (change > 0) {
            for (int i = 0; i < change; i++) {
                int port;
                if (url.getScheme().equals("https")) {
                    port = 443;
                } else if (url.getScheme().equals("http")) {
                    port = 80;
                } else {
                    port = url.getPort();
                }
                ChannelFuture connectFuture = bootstrap.connect(url.getHost(), port);
                connectFuture.addListener(future -> {
                    Channel channel = connectFuture.channel();
                    channels.add(channel);
                    sendForChannel(channel);
                });
            }
        }
    }

    private HttpRequest httpRequest() {
        HttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, url.getRawPath()
        );
        request.headers().set(HttpHeaderNames.HOST, url.getHost());
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        return request;
    }

    private void sendForChannel(Channel channel) {
        if (channels.contains(channel)) {
            channel.writeAndFlush(httpRequest()).addListener(future -> {
                if (future.isSuccess()) {
                    statisticsRecorder.recordRequestSent();
                    channelsInfo.putRequestSentTimeForChannel(channel.id());
                    if (channelsInfo.inflightRequestsForChannel(channel.id()) < 2) {
                        sendForChannel(channel);
                    } else {
                        vertx.setTimer(50, event -> sendForChannel(channel));
                    }
                } else {
                    log.error("Future failed");
                }
            });
        } else {
            log.info("Channel '{}' is removed, not sending for the channel", channel.id().asShortText());
        }
    }
}

package com.orderddos.network.netty;

import com.orderddos.network.StatisticsRecorder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpGetNettyResponseHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger log = LoggerFactory.getLogger(HttpGetNettyResponseHandler.class);

    private final StatisticsRecorder statisticsRecorder;
    private final ChannelsInfo channelsInfo;

    public HttpGetNettyResponseHandler(ChannelsInfo channelsInfo, StatisticsRecorder statisticsRecorder) {
        this.statisticsRecorder = statisticsRecorder;
        this.channelsInfo = channelsInfo;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) msg;
            ChannelId id = ctx.channel().id();
            long start = channelsInfo.pollRequestSentTimeForChannel(id);
            long stop = System.currentTimeMillis();
            long elapsed = stop - start;
            int code = httpResponse.status().code();
            statisticsRecorder.recordResponseReceived(elapsed, code);
        } else {
            log.error("An unknown thing is received: {}", msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Error occurred in HTTP response handling", cause);
    }
}

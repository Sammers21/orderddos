package com.orderddos.network.netty;

import com.orderddos.network.DecisionEngine;
import com.orderddos.network.NetworkLoader;
import com.orderddos.network.StatisticsRecorder;
import com.orderddos.network.decisions.Decision;
import com.orderddos.network.decisions.StatisticsRecorderImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;

import java.net.URI;

import static org.slf4j.LoggerFactory.getLogger;

public class HttpGetNettyNetworkLoader implements NetworkLoader {

    private static final Logger log = getLogger(HttpGetNettyNetworkLoader.class);

    @Override
    public void loadAddress(String address, DecisionEngine decisionEngine) throws Exception {
        URI url = new URI(address);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        EventLoopGroup eventExecutors = new NioEventLoopGroup(availableProcessors * 2);
        Vertx vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(1));

        log.info("Using {} epoll event loop threads", availableProcessors);
        final SslContext sslContext;
        if (address.startsWith("https")) {
            sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslContext = null;
        }
        Bootstrap b = new Bootstrap();
        ChannelsInfo channelsInfo = new ChannelsInfo();
        GlobalTrafficShapingHandler shapingHandler = new GlobalTrafficShapingHandler(GlobalEventExecutor.INSTANCE, 0, 0, 0);
        TrafficCounter trafficCounter = shapingHandler.trafficCounter();
        StatisticsRecorder statisticsRecorder = new StatisticsRecorderImpl(trafficCounter);
        b.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (sslContext != null) {
                            pipeline.addLast(sslContext.newHandler(ch.alloc()));
                        }

                        pipeline.addLast(shapingHandler);
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpContentDecompressor());
                        pipeline.addLast(new HttpObjectAggregator(1048576));
                        pipeline.addLast(new HttpGetNettyResponseHandler(channelsInfo, statisticsRecorder));
                    }
                });
        trafficCounter.start();
        NetworkController networkController = new NetworkController(vertx, b, url, channelsInfo, statisticsRecorder);
        vertx.setPeriodic(1000, event -> {
            statisticsRecorder.takeSnapshot(networkController.aliveConnections());
            Decision decision = decisionEngine.makeDecision(statisticsRecorder.lastStatistics());
            networkController.processDecision(decision);
        });
    }
}

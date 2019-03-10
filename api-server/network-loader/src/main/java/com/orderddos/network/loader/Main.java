package com.orderddos.network.loader;

import com.orderddos.network.decisions.ChangeAmountOfConnections;
import com.orderddos.network.netty.HttpGetNettyNetworkLoader;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpGetNettyNetworkLoader httpGetNettyNetworkLoader = new HttpGetNettyNetworkLoader();
        httpGetNettyNetworkLoader.loadAddress("https://order-ddos.com/",
                loadStatistics -> new ChangeAmountOfConnections(1)
        );
    }
}

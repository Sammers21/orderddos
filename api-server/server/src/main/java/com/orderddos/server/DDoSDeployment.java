package com.orderddos.server;

import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.*;
import com.orderddos.docean.DropletImage;
import com.orderddos.docean.DropletSize;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.orderddos.docean.DropletRegion.AMS_3;
import static com.orderddos.server.DDoSApiService.SETUP_ENV_SCRIPT;

public class DDoSDeployment {

    private static Logger log = LoggerFactory.getLogger(DDoSDeployment.class);

    private final DigitalOceanClient digitalOceanClient;
    private final Vertx vertx;
    private final Order order;
    private final Undeployer undeployer;

    private static final Integer DELAY_BEFORE_DEPLOYMENT_MS = (60 * 2 + 30) * 1000;

    public DDoSDeployment(DigitalOceanClient digitalOceanClient, Vertx vertx, Order order, Undeployer undeployer) {
        this.digitalOceanClient = digitalOceanClient;
        this.vertx = vertx;
        this.order = order;
        this.undeployer = undeployer;
    }

    public Future<Void> deploy() {
        Droplet droplets = droplets();
        Future<Droplets> fd = deployDroplets(droplets);
        removeDropletsAfter(duration() + DELAY_BEFORE_DEPLOYMENT_MS);
        return fd.mapEmpty();
    }

    private Future<Droplets> deployDroplets(Droplet droplets) {
        Future<Droplets> fd = Future.future();
        vertx.executeBlocking(event -> {
            try {
                Droplets createdDropltets = digitalOceanClient.createDroplets(droplets);
                log.info("Order '{}' is deployed: {}", order.getUuid(), order);
                event.complete(createdDropltets);
            } catch (Exception e) {
                log.error("Failed to deploy order " + order, e);
                event.fail(e);
            }
        }, fd);
        return fd;
    }

    private void removeDropletsAfter(int duration) {
        vertx.setTimer(duration, timeToUndeploy -> {
            undeployer.undeployAttackWithUUID(order.getUuid());
        });
    }

    private Droplet droplets() {
        String firstFourLettersOfUUid = order.getUuid().toString().substring(0, 4);
        Integer euDrop = order.getNum_nodes_by_region().getInteger("eu");

        List<String> dropletNames = IntStream.range(0, euDrop)
                .boxed()
                .map(number -> firstFourLettersOfUUid + "-instance-" + number)
                .collect(Collectors.toList());

        // Create a new droplets
        Droplet droplets = new Droplet();
        droplets.setNames(dropletNames);
        droplets.setSize(DropletSize.S_1_VCPU_1GB);
        droplets.setRegion(new Region(AMS_3));
        droplets.setImage(new Image(DropletImage.UBUNTU_18_04_X64));
        droplets.setTags(List.of(order.getUuid().toString()));
        final String userData = userData();
        droplets.setUserData(userData);
        log.info("User data:\n" + userData);
        List<Key> keys = new ArrayList<>();
        final Key key = new Key();
        key.setFingerprint("36:59:36:cf:aa:7f:0a:1a:e9:8f:18:b9:0b:5b:59:d2");
        keys.add(key);
        droplets.setKeys(keys);
        return droplets;
    }

    private int duration() {
        return (order.getDuration().getSeconds() +
                order.getDuration().getMinutes() * 60 +
                order.getDuration().getHours() * 60 * 60 +
                order.getDuration().getDays() * 60 * 60 * 24) * 1000;
    }

    private String userData() {
        return SETUP_ENV_SCRIPT + String.format(
                "\n  - wget https://download.java.net/java/GA/jdk11/13/GPL/openjdk-11.0.1_linux-x64_bin.tar.gz -O /root/openjdk-11.0.1_linux-x64_bin.tar.gz\n" +
                        "  - tar xzv -C /opt -f /root/openjdk-11.0.1_linux-x64_bin.tar.gz\n" +
                        "  - update-alternatives --install /usr/bin/java java /opt/jdk-11.0.1/bin/java 1\n" +
                        "  - wget https://github.com/order-ddos/order-ddos.github.io/releases/download/0.0.7/node-agent-0.0.7.jar -O /root/node-agent.jar\n" +
                        "  - mkdir -p /root/node-agent\n" +
                        "  - cd /root/node-agent\n" +
                        "  - java -jar /root/node-agent.jar %s %s &",
                order.getUuid().toString(),
                order.getTarget_url()
        );
    }
}

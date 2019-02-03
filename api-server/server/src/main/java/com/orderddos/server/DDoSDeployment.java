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

public class DDoSDeployment {

    private static Logger log = LoggerFactory.getLogger(DDoSDeployment.class);

    private final DigitalOceanClient digitalOceanClient;
    private final Vertx vertx;
    private final Order order;

    private static final Integer DELAY_BEFORE_DEPLOYMENT_MS = (60 * 2 + 30) * 1000;

    public DDoSDeployment(DigitalOceanClient digitalOceanClient, Vertx vertx, Order order) {
        this.digitalOceanClient = digitalOceanClient;
        this.vertx = vertx;
        this.order = order;
    }

    public Future<Void> deploy() {
        Droplet droplets = droplets();
        Future<Droplets> fd = deployDroplets(droplets);
        removeDropletsAfter(duration());
        deployViralNetworkAfter(DELAY_BEFORE_DEPLOYMENT_MS);
        return fd.mapEmpty();
    }

    private void deployViralNetworkAfter(Integer delayBeforeDeploymentMs) {
        vertx.setTimer(delayBeforeDeploymentMs, event -> {

        });
    }

    private Future<Droplets> deployDroplets(Droplet droplets) {
        Future<Droplets> fd = Future.future();
        vertx.executeBlocking(event -> {
            try {
                Droplets createdDropltets = digitalOceanClient.createDroplets(droplets);
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
            Future<Delete> delete = Future.future();
            vertx.executeBlocking(event -> {
                try {
                    event.complete(digitalOceanClient.deleteDropletByTagName(order.getUuid().toString()));
                } catch (Exception e) {
                    log.error("Failed to delete" + order, e);
                    event.fail(e);
                }
            }, delete);
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
        droplets.setUserData(DDoSApiService.SETUP_ENV_SCRIPT);
        List<Key> keys = new ArrayList<>();
        keys.add(new Key("36:59:36:cf:aa:7f:0a:1a:e9:8f:18:b9:0b:5b:59:d2"));
        droplets.setKeys(keys);
        return droplets;
    }

    private int duration() {
        return (order.getDuration().getSeconds() +
                order.getDuration().getMinutes() * 60 +
                order.getDuration().getHours() * 60 * 60 +
                order.getDuration().getDays() * 60 * 60 * 24) * 1000;
    }
}

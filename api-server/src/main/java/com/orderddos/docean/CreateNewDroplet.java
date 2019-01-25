package com.orderddos.docean;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class CreateNewDroplet {
    private final String name;
    private final String region;
    private final String size;
    private final String image;

    private List<String> ssh_keys = null;
    private Boolean backups = null;
    private Boolean ipv6 = null;
    private Boolean private_networking = null;
    private String user_data = null;
    private Boolean monitoring = null;
    private List<String> volumes = null;
    private List<String> tags = null;

    public CreateNewDroplet(String name, String region, String size, String image) {
        this.name = name;
        this.region = region;
        this.size = size;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public String getSize() {
        return size;
    }

    public String getImage() {
        return image;
    }

    public List<String> getSsh_keys() {
        return ssh_keys;
    }

    public List<String> getVolumes() {
        return volumes;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setSsh_keys(List<String> ssh_keys) {
        this.ssh_keys = ssh_keys;
    }

    public void setVolumes(List<String> volumes) {
        this.volumes = volumes;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}

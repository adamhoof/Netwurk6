package io;

import common.NetworkDeviceType;

import java.util.UUID;

public class NetworkDeviceViewDTO {
    private UUID uuid;
    private String name;
    private double x;
    private double y;
    private NetworkDeviceType type;

    public NetworkDeviceViewDTO(UUID uuid, String name, double x, double y, NetworkDeviceType type) {
        this.uuid = uuid;
        this.name = name;
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public NetworkDeviceType getType() {
        return type;
    }

    public void setType(NetworkDeviceType type) {
        this.type = type;
    }
}

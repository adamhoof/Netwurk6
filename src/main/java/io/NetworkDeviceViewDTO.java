package io;

import common.NetworkDeviceType;

import java.util.UUID;

public record NetworkDeviceViewDTO(UUID uuid, String name, double x, double y, NetworkDeviceType type) {


}

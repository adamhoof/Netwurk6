package io;

import java.util.List;

public record NetworkData(List<NetworkDeviceViewDTO> devices, List<ConnectionLineDTO> connections,
                          AutoNameGeneratorDTO autoNameGeneratorDTO) {
}
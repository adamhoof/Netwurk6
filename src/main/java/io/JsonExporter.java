package io;

import view.NetworkDeviceView;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonExporter {
    private final ObjectMapper mapper = new ObjectMapper();

    public String exportDevices(List<NetworkDeviceView> devices) throws IOException {
        List<NetworkDeviceViewDTO> dtos = devices.stream()
                .map(device -> new NetworkDeviceViewDTO(
                        device.getUuid(),
                        device.getName(),
                        device.getLayoutX(),
                        device.getLayoutY(),
                        device.getNetworkDeviceType()))
                .collect(Collectors.toList());

        return mapper.writeValueAsString(dtos);
    }
}

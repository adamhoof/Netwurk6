package io;

import view.NetworkDeviceView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonExporter {

    private final ObjectMapper mapper = new ObjectMapper();

    public void exportNetworkData(List<NetworkDeviceView> devices, List<ConnectionLineDTO> connections, File file) throws IOException {
        NetworkData exportData = new NetworkData(devices.stream()
                .map(device -> new NetworkDeviceViewDTO(
                        device.getUuid(),
                        device.getName(),
                        device.getLayoutX(),
                        device.getLayoutY(),
                        device.getNetworkDeviceType()
                ))
                .collect(Collectors.toList()),
                connections);

        Files.write(Paths.get(file.toURI()), mapper.writeValueAsString(exportData).getBytes());
    }

    public record NetworkData(List<NetworkDeviceViewDTO> devices, List<ConnectionLineDTO> connections) {
    }
}

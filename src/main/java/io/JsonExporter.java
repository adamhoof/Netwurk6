package io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonExporter {

    private final ObjectMapper mapper = new ObjectMapper();

    public void exportNetworkData(List<NetworkDeviceViewDTO> devices, List<ConnectionLineDTO> connections, AutoNameGeneratorDTO autoNameGenerator, File file) throws IOException {
        NetworkData exportData = new NetworkData(
                devices,
                connections,
                autoNameGenerator);

        Files.write(Paths.get(file.toURI()), mapper.writeValueAsString(exportData).getBytes());
    }
}

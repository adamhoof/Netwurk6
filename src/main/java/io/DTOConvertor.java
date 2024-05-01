package io;

import common.AutoNameGenerator;
import view.ConnectionLine;
import view.NetworkDeviceView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DTOConvertor {
    public List<ConnectionLineDTO> convertConnectionLinesToDTOs(List<ConnectionLine> connectionLines) {
        List<ConnectionLineDTO> connectionDTOs = new ArrayList<>();
        for (ConnectionLine connectionLine : connectionLines) {

            UUID startDeviceUuid = connectionLine.getStartDevice().getUuid();
            UUID endDeviceUuid = connectionLine.getEndDevice().getUuid();

            ConnectionLineDTO dto = new ConnectionLineDTO(startDeviceUuid, endDeviceUuid);
            connectionDTOs.add(dto);
        }
        return connectionDTOs;
    }

    public List<NetworkDeviceViewDTO> convertNetworkDeviceViewsToDTOs(List<NetworkDeviceView> networkDeviceViews) {
        return networkDeviceViews.stream()
                .map(device -> new NetworkDeviceViewDTO(
                        device.getUuid(),
                        device.getName(),
                        device.getLayoutX(),
                        device.getLayoutY(),
                        device.getNetworkDeviceType()
                ))
                .collect(Collectors.toList());
    }

    public AutoNameGeneratorDTO convertAutoNameGeneratorToDTO(AutoNameGenerator autoNameGenerator) {
        return new AutoNameGeneratorDTO(AutoNameGenerator.getInstance().getRouterNameCounter(), AutoNameGenerator.getInstance().getSwitchNameCounter(), AutoNameGenerator.getInstance().getRouterInterfaceNameCounter(), AutoNameGenerator.getInstance().getPcNameCounter());
    }
}

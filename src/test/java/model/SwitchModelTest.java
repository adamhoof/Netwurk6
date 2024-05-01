package model;

import common.AutoNameGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class SwitchModelTest {
    @Test
    public void learnMacAddress() {
        SwitchModel switchModel = new SwitchModel(UUID.randomUUID(), new MACAddress(UUID.randomUUID().toString()), AutoNameGenerator.getInstance().generateSwitchName());
        PCModel toRemove = new PCModel(UUID.randomUUID(), new MACAddress(UUID.randomUUID().toString()), AutoNameGenerator.getInstance().generatePcName());

        switchModel.addConnection(toRemove);
        SwitchConnection switchConnection = switchModel.getSwitchConnections().iterator().next();
        Assertions.assertNotNull(switchConnection);

        switchModel.learnMacAddress(switchConnection.getNetworkDeviceModel().getMacAddress(), switchConnection.getPort());
        Assertions.assertEquals(1, switchModel.getSwitchConnections().size());
    }

    @Test
    public void forgetMacAddress() {
        SwitchModel switchModel = new SwitchModel(UUID.randomUUID(), new MACAddress(UUID.randomUUID().toString()), AutoNameGenerator.getInstance().generateSwitchName());
        PCModel toRemove = new PCModel(UUID.randomUUID(), new MACAddress(UUID.randomUUID().toString()), AutoNameGenerator.getInstance().generatePcName());

        switchModel.addConnection(toRemove);
        SwitchConnection switchConnection = switchModel.getSwitchConnections().iterator().next();
        Assertions.assertNotNull(switchConnection);

        switchModel.learnMacAddress(switchConnection.getNetworkDeviceModel().getMacAddress(), switchConnection.getPort());

        switchModel.forgetMacAddress(toRemove.getMacAddress());

        Assertions.assertEquals(1, switchModel.getSwitchConnections().size());
        Assertions.assertEquals(0, switchModel.getCamTable().getEntries().size());
    }
}

import controller.NetworksController;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class NetworksControllerTest {

    @Test
    public void getCurrentAvailableWanLinkNetworkAddress_fourth_octet_increment_by_4() {
        NetworksController networksController = new NetworksController();
        IPAddress expected = new IPAddress(50, 0, 0, 4);

        IPAddress reserved = networksController.reserveCurrentAvailableWanLinkNetworkAddress();
        IPAddress result = networksController.reserveCurrentAvailableWanLinkNetworkAddress();

        Assertions.assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void addPcNetworkConnection_pcConnectionsNotEmpty_returnFalse() {
        NetworksController networksController = new NetworksController();
        PCModel pc = new PCModel(UUID.randomUUID(), new MACAddress("PC1_MAC"));
        SwitchModel switchModel = new SwitchModel(UUID.randomUUID(), new MACAddress("SWITCH_MAC"));
        RouterModel router = new RouterModel(UUID.randomUUID(), new MACAddress("ROUTER_MAC"));

        pc.addConnection(switchModel);

        boolean expected = false;
        boolean result = pc.addConnection(router);

        int expectedPcNetworkConnectionsSize = 1;
        int actualPcNetworkConnections = pc.getDirectConnections().size();
        Assertions.assertEquals(expected, result);
        Assertions.assertEquals(expectedPcNetworkConnectionsSize, actualPcNetworkConnections);
    }
}
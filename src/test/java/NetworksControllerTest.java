import controller.NetworksController;
import model.IPAddress;
import model.MACAddress;
import model.PCModel;
import model.RouterModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

public class NetworksControllerTest {

    @Test
    public void getCurrentAvailableWanLinkNetworkAddress_fourth_octet_increment_by_4() {
        NetworksController networksController = new NetworksController();
        IPAddress expected = new IPAddress(50, 0, 0, 4);

        IPAddress reserved = networksController.reserveCurrentAvailableWanLinkNetworkAddress();
        IPAddress result = networksController.reserveCurrentAvailableWanLinkNetworkAddress();

        Assertions.assertEquals(Arrays.toString(expected.getOctets()), Arrays.toString(result.getOctets()));
    }

    @Test
    public void addNetworkConnection_firstPC_connectionsNotEmpty_returnFalse() {
        NetworksController networksController = new NetworksController();
        PCModel pc1 = new PCModel(UUID.randomUUID(), new MACAddress("PC1_MAC"));
        PCModel pc2 = new PCModel(UUID.randomUUID(), new MACAddress("PC2_MAC"));
        RouterModel router = new RouterModel(UUID.randomUUID(), new MACAddress("ROUTER_MAC"));
        networksController.addNetworkConnection(pc1, router);

        boolean expected = false;
        boolean result = networksController.addNetworkConnection(pc1, pc2);

        Assertions.assertEquals(expected, result);
    }
}
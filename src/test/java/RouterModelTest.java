import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class RouterModelTest {

    @Test
    public void createLanNetwork() {
        RouterModel routerModel = new RouterModel(UUID.randomUUID(), new MACAddress(UUID.randomUUID().toString()));

        IPAddress networkIp = new IPAddress(routerModel.getCurrentAvailableLanNetworkIp());
        routerModel.getCurrentAvailableLanNetworkIp().incrementOctet(3, 1);

        LanNetwork lanNetwork = new LanNetwork(networkIp, new SubnetMask(24));
        routerModel.getLanNetworks().add(lanNetwork);

        IPAddress interfaceIp = lanNetwork.getNextAvailableIpAddress();
        RouterInterface routerInterface = new RouterInterface(interfaceIp, new MACAddress(UUID.randomUUID().toString()));
        routerModel.getRouterInterfaces().put(lanNetwork, routerInterface);

        IPAddress expectedCurrentAvailableIp = new IPAddress(192,168,2,0);
        int expectedRouterModelLanNetworksCount = 1;
        int expectedRouterModelInterfacesCount = 1;

        Assertions.assertEquals(expectedCurrentAvailableIp.toString(), routerModel.getCurrentAvailableLanNetworkIp().toString());
        Assertions.assertEquals(expectedRouterModelLanNetworksCount, routerModel.getLanNetworks().size());
        Assertions.assertEquals(expectedRouterModelInterfacesCount,routerModel.getRouterInterfaces().size());
    }
}

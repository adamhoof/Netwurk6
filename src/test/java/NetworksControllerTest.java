import model.IPAddress;
import model.NetworksController;
import model.SubnetMask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class NetworksControllerTest {

    @Test
    public void getCurrentAvailableWanLinkNetworkAddress_fourth_octet_increment_by_4() {
        NetworksController networksController = new NetworksController();
        networksController.setCurrentAvailableWanNetworkAddress(new IPAddress(50, 0, 0, 0));
        networksController.setDefaultWanRouterLinkSubnetMask(new SubnetMask(30));
        IPAddress expected = new IPAddress(50, 0, 0, 4);

        networksController.reserveCurrentAvailableWanLinkNetworkAddress();
        IPAddress result = networksController.getCurrentAvailableWanNetworkAddress();

        Assertions.assertEquals(Arrays.toString(expected.getOctets()), Arrays.toString(result.getOctets()));
    }

    @Test
    public void getCurrentAvailableWanLinkNetworkAddress_border_octet_increment_by_1() {
        NetworksController networksController = new NetworksController();
        networksController.setCurrentAvailableWanNetworkAddress(new IPAddress(50, 0, 0, 0));
        networksController.setDefaultWanRouterLinkSubnetMask(new SubnetMask(24));
        IPAddress expected = new IPAddress(50,0,1,0);

        networksController.reserveCurrentAvailableWanLinkNetworkAddress();
        IPAddress result = networksController.getCurrentAvailableWanNetworkAddress();

        Assertions.assertEquals(Arrays.toString(expected.getOctets()), Arrays.toString(result.getOctets()));
    }
}

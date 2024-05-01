package controller;

import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NetworksControllerTest {

    @Test
    public void getCurrentAvailableWanLinkNetworkAddress_fourth_octet_increment_by_4() {
        NetworksController networksController = new NetworksController();
        IPAddress expected = new IPAddress(50, 0, 0, 4);

        IPAddress reserved = networksController.reserveCurrentAvailableWanLinkNetworkAddress();
        IPAddress result = networksController.reserveCurrentAvailableWanLinkNetworkAddress();

        Assertions.assertEquals(expected.toString(), result.toString());
    }
}
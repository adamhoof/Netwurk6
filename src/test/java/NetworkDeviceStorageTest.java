import model.MACAddress;
import model.NetworkDeviceModel;
import model.NetworkDeviceStorage;
import model.RouterModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class NetworkDeviceStorageTest {

    @Test
    public void getNetworkDeviceByMac_deviceExists_returnDevice() {

        NetworkDeviceStorage storage = new NetworkDeviceStorage();
        UUID uuid = UUID.randomUUID();
        RouterModel routerModel = new RouterModel(uuid, new MACAddress(uuid.toString()));

        storage.add(routerModel);

        NetworkDeviceModel resultModel = storage.getNetworkDeviceByMac(routerModel.getMacAddress());

        Assertions.assertEquals(routerModel, resultModel);
    }
}

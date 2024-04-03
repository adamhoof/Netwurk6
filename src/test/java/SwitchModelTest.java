import model.MACAddress;
import model.SwitchModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class SwitchModelTest {
    @Test
    public void removeEntry__addEntryPickupCorrectPort() {
        SwitchModel switchModel = new SwitchModel(UUID.randomUUID(), new MACAddress(UUID.randomUUID().toString()));

        MACAddress toRemove = new MACAddress(UUID.randomUUID().toString());
        switchModel.learnMacAddress(toRemove);
        switchModel.learnMacAddress(new MACAddress(UUID.randomUUID().toString()));
        switchModel.forgetMacAddress(toRemove);

        int expected = 1;
        int actual = switchModel.getCamTable().getEntries().size();

        Assertions.assertEquals(expected, actual);
    }
}

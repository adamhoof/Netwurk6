package model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IPAddressTest {
    @Test
    public void toLongTest(){
        IPAddress ipAddress = new IPAddress(192,168,1,0);

        long expected = 3232235776L;
        long result = ipAddress.toLong();

        Assertions.assertEquals(expected,result);
    }
}

package util;

import org.junit.Test;

import java.util.UUID;

public class UUIDTest {
    @Test
    public void uuid() {
        UUID uuid = UUID.randomUUID();
        System.out.println("uuid = " + uuid);
    }
}

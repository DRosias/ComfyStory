package net.swordie.webapi.routes;

import jakarta.ws.rs.BadRequestException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LauncherAuthRouteTest {
    @Test
    public void usernameIsTrimmed() {
        assertEquals("player", LauncherAuthRoute.normalizeAndValidateUsername("  player  "));
    }

    @Test(expected = BadRequestException.class)
    public void shortUsernameIsRejected() {
        LauncherAuthRoute.normalizeAndValidateUsername("abc");
    }

    @Test
    public void minimumPasswordIsAccepted() {
        LauncherAuthRoute.validatePassword("123456");
    }

    @Test(expected = BadRequestException.class)
    public void shortPasswordIsRejected() {
        LauncherAuthRoute.validatePassword("12345");
    }
}

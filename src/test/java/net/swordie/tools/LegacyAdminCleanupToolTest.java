package net.swordie.tools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LegacyAdminCleanupToolTest {
    @Test
    public void usernameIsTrimmed() throws Exception {
        assertEquals("privateAdmin", LegacyAdminCleanupTool.validateUsername("  privateAdmin  "));
    }

    @Test(expected = LegacyAdminCleanupTool.CleanupException.class)
    public void shortUsernameIsRejected() throws Exception {
        LegacyAdminCleanupTool.validateUsername("abc");
    }

    @Test
    public void eightCharacterPasswordIsAccepted() throws Exception {
        LegacyAdminCleanupTool.validatePassword("12345678".toCharArray());
    }

    @Test(expected = LegacyAdminCleanupTool.CleanupException.class)
    public void shortPasswordIsRejected() throws Exception {
        LegacyAdminCleanupTool.validatePassword("1234567".toCharArray());
    }
}

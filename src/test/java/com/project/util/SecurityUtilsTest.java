package com.project.util;

import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityUtilsTest {
    private String createFakeToken(String sub, String role) {
        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"none\"}".getBytes());
        String payload = Base64.getUrlEncoder().encodeToString(
                ("{\"sub\":\"" + sub + "\", \"role\":\"" + role + "\"}").getBytes()
        );
        return "Bearer " + header + "." + payload + ".signature";
    }

    @Test
    void getRoleFromToken_ValidAdmin_ReturnsAdmin() {
        String token = createFakeToken("user_123", "ADMIN");
        assertEquals("ADMIN", SecurityUtils.getRoleFromToken(token));
    }

    @Test
    void getRoleFromToken_ValidUser_ReturnsUser() {
        String token = createFakeToken("user_123", "USER");
        assertEquals("USER", SecurityUtils.getRoleFromToken(token));
    }

    @Test
    void getRoleFromToken_InvalidToken_ReturnsDefaultUser() {
        assertEquals("USER", SecurityUtils.getRoleFromToken("Invalid.Token"));
    }

    @Test
    void getUserIdFromToken_ValidUser_ReturnsId() {
        String token = createFakeToken("buyer_99", "USER");
        assertEquals("buyer_99", SecurityUtils.getUserIdFromToken(token));
    }

    @Test
    void getUserIdFromToken_InvalidToken_ReturnsDefaultUnknown() {
        assertEquals("unknown_user", SecurityUtils.getUserIdFromToken("Not.A.Token"));
    }
}
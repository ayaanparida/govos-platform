package com.govos.security.support;

import com.govos.security.annotation.CurrentUser;
import com.govos.security.jwt.JwtPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestSecuredController {

    @GetMapping("/api/v1/test/me")
    ResponseEntity<Map<String, Object>> currentUser(@CurrentUser JwtPrincipal user) {
        return ResponseEntity.ok(Map.of(
                "userId", user.getUserId().toString(),
                "username", user.getUsername()));
    }

    @GetMapping("/api/v1/test/protected")
    ResponseEntity<Void> protectedEndpoint() {
        return ResponseEntity.ok().build();
    }
}

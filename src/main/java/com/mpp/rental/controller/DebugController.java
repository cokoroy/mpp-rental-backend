package com.mpp.rental.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final PasswordEncoder passwordEncoder;

    @GetMapping("/authorities")
    public ResponseEntity<Map<String, Object>> debugAuthorities() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> debug = new HashMap<>();
        debug.put("authenticated", auth.isAuthenticated());
        debug.put("name", auth.getName());
        debug.put("authorities", auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        debug.put("principal", auth.getPrincipal().toString());
        return ResponseEntity.ok(debug);
    }

    // ── TEMPORARY: generates a BCrypt hash using YOUR app's encoder ──
    // Call: GET /api/debug/hash?password=yourpassword
    // Copy the hash, update DB, then delete this endpoint
    @GetMapping("/hash")
    public ResponseEntity<Map<String, String>> generateHash(@RequestParam String password) {
        Map<String, String> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", passwordEncoder.encode(password));
        return ResponseEntity.ok(result);
    }
}
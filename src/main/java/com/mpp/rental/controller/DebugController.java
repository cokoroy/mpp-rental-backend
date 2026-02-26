package com.mpp.rental.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

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
}
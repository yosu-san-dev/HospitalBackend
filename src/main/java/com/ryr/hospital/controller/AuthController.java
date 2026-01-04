package com.ryr.hospital.controller;

import com.ryr.hospital.service.AuthService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> body) {
        try {
            String name = body.get("fullName");
            String tc = body.get("tc");
            String pass = body.get("password");

            authService.register(name, tc, pass);
            return "User registered successfully!";
        } catch(Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> body) {
        try {
            String tc = body.get("tc");
            String pass = body.get("password");

            String token = authService.login(tc, pass);
            return token;
        } catch(Exception e) {
            return "Login Failed: " + e.getMessage();
        }
    }
}

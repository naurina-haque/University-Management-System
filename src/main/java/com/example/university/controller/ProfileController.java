package com.example.university.controller;

import com.example.university.dto.Dtos.*;
import com.example.university.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService service;

    @GetMapping
    public UserResponse me(Authentication auth) {
        return service.me(auth.getName());
    }

    @PutMapping
    public UserResponse update(Authentication auth, @Valid @RequestBody SelfUpdateRequest request) {

        return service.updateMe(auth.getName(), request);
    }
}
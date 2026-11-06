package com.example.university.dto;

import com.example.university.model.Role;
import com.example.university.model.UserAccount;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Dtos {

    // Admin create
    public record CreateUserRequest(
            @NotBlank String username,
            @NotBlank @Size(min = 6) String password,
            @NotNull Role role,
            @NotBlank String fullName,
            @Email String email,
            String phone,
            String department) {}

    // Admin update
    public record UpdateUserRequest(
            @NotBlank String fullName,
            @Email String email,
            String phone,
            String department,
            @Size(min = 6) String password) {}   // optional

    // self info update
    public record SelfUpdateRequest(
            @NotBlank String fullName,
            @Email String email,
            String phone,
            @Size(min = 6) String password) {}   // optional

    public record UserResponse(Long id, String username, Role role,
                               String fullName, String email,
                               String phone, String department) {
        public static UserResponse from(UserAccount u) {
            return new UserResponse(u.getId(), u.getUsername(), u.getRole(),
                    u.getFullName(), u.getEmail(), u.getPhone(), u.getDepartment());
        }
    }
    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password) {}

    public record LoginResponse(String token, String type, String username, Role role) {}
}
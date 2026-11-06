package com.example.university.service;

import com.example.university.dto.Dtos.*;
import com.example.university.model.Role;
import com.example.university.model.UserAccount;
import com.example.university.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    // ---------- ADMIN operations ----------

    public UserResponse create(CreateUserRequest r) {
        if (r.role() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only TEACHER or STUDENT can be created");
        }
        if (repo.existsByUsername(r.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        UserAccount u = new UserAccount();
        u.setUsername(r.username());
        u.setPassword(encoder.encode(r.password()));
        u.setRole(r.role());
        u.setFullName(r.fullName());
        u.setEmail(r.email());
        u.setPhone(r.phone());
        u.setDepartment(r.department());
        return UserResponse.from(repo.save(u));
    }

    public List<UserResponse> list(Role role) {
        List<UserAccount> users = (role == null) ? repo.findAll() : repo.findByRole(role);
        return users.stream().map(UserResponse::from).toList();
    }

    public UserResponse get(Long id) {
        return UserResponse.from(findManaged(id));
    }

    public UserResponse update(Long id, UpdateUserRequest r) {
        UserAccount u = findManaged(id);
        u.setFullName(r.fullName());
        u.setEmail(r.email());
        u.setPhone(r.phone());
        u.setDepartment(r.department());
        if (r.password() != null && !r.password().isBlank()) {
            u.setPassword(encoder.encode(r.password()));
        }
        return UserResponse.from(repo.save(u));
    }

    public void delete(Long id) {
        repo.delete(findManaged(id));
    }

    // ---------- Self operations (teacher / student / admin) ----------

    public UserResponse me(String username) {
        return UserResponse.from(findByUsername(username));
    }

    public UserResponse updateMe(String username, SelfUpdateRequest r) {
        UserAccount u = findByUsername(username);
        u.setFullName(r.fullName());
        u.setEmail(r.email());
        u.setPhone(r.phone());
        if (r.password() != null && !r.password().isBlank()) {
            u.setPassword(encoder.encode(r.password()));
        }
        return UserResponse.from(repo.save(u));
    }

    // ---------- helpers ----------

    private UserAccount findByUsername(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserAccount findManaged(Long id) {
        UserAccount u = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (u.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin account cannot be managed here");
        }
        return u;
    }
}
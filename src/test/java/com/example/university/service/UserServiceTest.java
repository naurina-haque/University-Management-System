package com.example.university.service;

import com.example.university.dto.Dtos.CreateUserRequest;
import com.example.university.model.Role;
import com.example.university.model.UserAccount;
import com.example.university.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService service;

    // tomar CreateUserRequest e role request er bhetore thake
    private CreateUserRequest request(Role role) {
        return new CreateUserRequest("t1", "123456", role, "Teacher One", "t1@uni.edu", "0170000000", "CSE");
    }

    // Test 1
    @Test
    void create_shouldHashPasswordAndSaveUserWithGivenRole() {
        when(repo.existsByUsername("t1")).thenReturn(false);
        when(encoder.encode("123456")).thenReturn("HASHED");
        when(repo.save(any(UserAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.create(request(Role.TEACHER));

        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("HASHED");   // plain password save hoy ni
        assertThat(captor.getValue().getRole()).isEqualTo(Role.TEACHER);
        assertThat(response.username()).isEqualTo("t1");
    }

    // Test 2
    @Test
    void create_shouldThrowConflictWhenUsernameAlreadyExists() {
        when(repo.existsByUsername("t1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(Role.TEACHER)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(409));
        verify(repo, never()).save(any(UserAccount.class));
    }

    // Test 3
    @Test
    void create_shouldRejectAdminRole() {
        assertThatThrownBy(() -> service.create(request(Role.ADMIN)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(400));
        verify(repo, never()).save(any(UserAccount.class));
    }

    // Test 4
    @Test
    void delete_shouldForbidManagingAdminAccount() {
        UserAccount admin = new UserAccount();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);
        when(repo.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(403));
        verify(repo, never()).delete(any(UserAccount.class));
    }
}
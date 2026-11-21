
package innowise.bankingApp.controller;

import innowise.bankingApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository users;

    @GetMapping("/internal/users/{id}")
    public ResponseEntity<UserStatus> getUserStatus(@PathVariable Long id) {
        return users.findById(id)
                .map(user -> ResponseEntity.ok()
                        .cacheControl(CacheControl.noStore())
                        .body(new UserStatus(user.isActive())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record UserStatus(boolean active) {
    }
}

package innowise.bankingApp.controller;

import innowise.bankingApp.model.dto.request.CreateOwnPaymentCardDto;
import innowise.bankingApp.model.dto.request.UpdateOwnPaymentCardDto;
import innowise.bankingApp.model.dto.request.UpdateUserDto;
import innowise.bankingApp.model.dto.response.ResponsePaymentCardDto;
import innowise.bankingApp.model.dto.response.ResponseUserDto;
import innowise.bankingApp.model.dto.response.ResponseUserWithCardsDto;
import innowise.bankingApp.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
public class CurrentUserController {

    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<ResponseUserDto> getUser(Principal principal) {
        return ResponseEntity.ok(currentUserService.getUser(userId(principal)));
    }

    @GetMapping("/with-cards")
    public ResponseEntity<ResponseUserWithCardsDto> getUserWithCards(Principal principal) {
        return ResponseEntity.ok(currentUserService.getUserWithCards(userId(principal)));
    }

    @PutMapping
    public ResponseEntity<ResponseUserDto> updateUser(Principal principal, @Valid @RequestBody UpdateUserDto dto) {
        return ResponseEntity.ok(currentUserService.updateUser(userId(principal), dto));
    }

    @GetMapping("/cards")
    public ResponseEntity<List<ResponsePaymentCardDto>> getCards(Principal principal) {
        return ResponseEntity.ok(currentUserService.getCards(userId(principal)));
    }

    @PostMapping("/cards")
    public ResponseEntity<ResponsePaymentCardDto> createCard(Principal principal, @Valid @RequestBody CreateOwnPaymentCardDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(currentUserService.createCard(userId(principal), dto));
    }

    @GetMapping("/cards/{cardId}")
    public ResponseEntity<ResponsePaymentCardDto> getCard(Principal principal, @PathVariable Long cardId) {
        return ResponseEntity.ok(currentUserService.getCard(userId(principal), cardId));
    }

    @PutMapping("/cards/{cardId}")
    public ResponseEntity<ResponsePaymentCardDto> updateCard(Principal principal, @PathVariable Long cardId, @Valid @RequestBody UpdateOwnPaymentCardDto dto) {
        return ResponseEntity.ok(currentUserService.updateCard(userId(principal), cardId, dto));
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(Principal principal, @PathVariable Long cardId) {
        currentUserService.deleteCard(userId(principal), cardId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/cards/{cardId}/activate")
    public ResponseEntity<ResponsePaymentCardDto> activateCard(Principal principal, @PathVariable Long cardId) {
        return ResponseEntity.ok(currentUserService.activateCard(userId(principal), cardId));
    }

    @PutMapping("/cards/{cardId}/deactivate")
    public ResponseEntity<ResponsePaymentCardDto> deactivateCard(Principal principal, @PathVariable Long cardId) {
        return ResponseEntity.ok(currentUserService.deactivateCard(userId(principal), cardId));
    }

    private static Long userId(Principal principal) {
        return Long.valueOf(principal.getName());
    }
}
package com.spring.api.API.Controllers;

import com.spring.api.API.models.DTOs.User.CreateUserDTO;
import com.spring.api.API.models.DTOs.User.UpdateUserDTO;
import com.spring.api.API.services.UserService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UserController {

    private final UserService service;

    public UserController(UserService userService) {
        this.service = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> create(@Valid @RequestBody CreateUserDTO user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(user));
    }

    @GetMapping("/find/id")
    public ResponseEntity<?> getUserById(@RequestParam() Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.findById(id));
    }

    @GetMapping("/find/username")
    public ResponseEntity<?> getUserByUsername(@RequestParam() String username) {
        return ResponseEntity.status(HttpStatus.OK).body(service.findByUsername(username));
    }

    @PatchMapping("/")
    public ResponseEntity<?> updateByUsername(@Valid @RequestBody() UpdateUserDTO new_data, @NonNull Authentication auth) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.service.updateUser(new_data, auth.getName()));
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<?> blockUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(this.service.blockUser(userId, user));
    }

    @DeleteMapping("/{userId}/block")
    public ResponseEntity<?> unblockUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(this.service.unblockUser(userId, user));
    }

    @GetMapping("/block")
    public ResponseEntity<?> blockedUsersList(@AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(this.service.usersBlockedList(user));
    }

    @GetMapping("/find/{text}")
    public ResponseEntity<?> findUsersByText(@PathVariable("text") String text, @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(this.service.usersFoundByText(text, user));
    }
}
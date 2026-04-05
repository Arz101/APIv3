package com.spring.api.API.Controllers;

import com.spring.api.API.models.DTOs.Auth.AccountTokenRequest;
import com.spring.api.API.models.DTOs.Auth.AuthRequest;
import com.spring.api.API.models.DTOs.Auth.AuthResponse;
import com.spring.api.API.models.DTOs.Auth.TokenRequest;
import com.spring.api.API.services.JWTService;
import com.spring.api.API.services.TokenService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          JWTService jwtService,
                          UserDetailsService userDetailsService,
                          TokenService tokenService){
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody() @NonNull AuthRequest login){
        long start = System.currentTimeMillis();

        org.springframework.security.core.Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
                login.username(),
                login.password()
            )
        );

        UserDetails user = (UserDetails) auth.getPrincipal();

        System.out.println("AUTH: " + (System.currentTimeMillis() - start));
        return ResponseEntity.status(HttpStatus.OK).body(new AuthResponse(
                jwtService.generateToken(user),
                this.tokenService.create(login.username())
        ));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody() @NonNull TokenRequest token){
        String username = this.tokenService.validate_refresh_token(token.refresh_token());
        UserDetails user =
                userDetailsService.loadUserByUsername(username);

        return ResponseEntity.status(HttpStatus.OK).body(new AuthResponse(
                jwtService.generateToken(user),
                this.tokenService.create(username)
        ));
    }

    @PostMapping("/active-account")
    public ResponseEntity<?> activeAccount(@Valid @RequestBody @NonNull AccountTokenRequest email_token) {
        this.tokenService.validateEmailToken(email_token.token());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","Account activated successfully"));
    }
    
    @PostMapping("reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody String entity) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of("message","No implemented yet :'("));
    }
    
}

package com.livevote.controller;

import com.livevote.dto.LoginRequest;
import com.livevote.dto.Response;
import com.livevote.service.interfac.UserServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserServiceInterface userService;

    @PostMapping("/login")
    @CrossOrigin(origins = "http://localhost:5173") // Allow requests from the front-end origin
    public ResponseEntity<Response> login(@RequestBody LoginRequest loginRequest) {
        log.info("/login");
        Response response = userService.login(loginRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}


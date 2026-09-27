package com.gamegrind.dev.AuthApplication.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/")
public class HelloController {

    @GetMapping("/")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Status : Healthy ");
    }

}

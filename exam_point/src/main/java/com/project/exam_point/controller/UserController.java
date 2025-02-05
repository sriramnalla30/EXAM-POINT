package com.project.exam_point.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.exam_point.model.User;
import com.project.exam_point.repository.UserRepository;
import com.project.exam_point.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user){
        try {
            // Check if the email is already in use
            if (userRepository.findByEmail(user.getEmail()) != null) {
                return ResponseEntity.badRequest().body("Email is already in use");
            }
            // Register the new user
            User newUser = userService.registerUser(user);
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Login endpoint to verify user
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        try {
            // Find the user by email
            User existingUser = userRepository.findByEmail(user.getEmail());
            if (existingUser == null) {
                return ResponseEntity.status(404).body("User not found");
            }

            // Check if the password matches
            if (!existingUser.getPassword().equals(user.getPassword())) {
                return ResponseEntity.status(401).body("Invalid password");
            }

            // Return user data with role info
            return ResponseEntity.ok(existingUser);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error");
        }
    }
}

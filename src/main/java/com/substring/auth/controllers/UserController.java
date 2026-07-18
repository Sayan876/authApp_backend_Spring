package com.substring.auth.controllers;

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

import com.substring.auth.dtos.UserDto;
import com.substring.auth.repositories.UserRepository;
import com.substring.auth.services.UserService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {
	
	private final UserService userService;

	private final UserRepository userRepository;
	
	
	
	@PostMapping
	public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto){
		return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
	} 
	
	
	@GetMapping
	public ResponseEntity<Iterable<UserDto>> getAllUsers(){
		return ResponseEntity.ok(userService.getAllUsers());
	}
	
	
	@GetMapping("/email/{email}")
	public ResponseEntity<UserDto> getUserByEmail(@PathVariable("email") String email){
		return ResponseEntity.ok(userService.getUserByEmail(email));
	}
	
	
	@DeleteMapping("/{userId}")
	public void deleteUser(@PathVariable("userId") String userId) {
		userService.deleteUser(userId);
	}
	
	
	@PutMapping("/{userId}")
	public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto, @PathVariable("userId") String userId){
		return ResponseEntity.ok(userService.updateUser(userDto,userId));
	}
	
	
	@GetMapping("byUserId/{userId}")
	public ResponseEntity<UserDto> getUserById(@PathVariable("userId") String userId){
		return ResponseEntity.ok(userService.findById(userId));
	}

}

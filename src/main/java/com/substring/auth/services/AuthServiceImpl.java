package com.substring.auth.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.substring.auth.dtos.UserDto;
import com.substring.auth.exceptions.ResourceNotFoundException;
import com.substring.auth.repositories.UserRepository;

import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService{
	
	private final UserService userService; 
	private final PasswordEncoder passwordEncoder; 
	
	

	@Override
	public UserDto registerUser(UserDto userDto) {
		// TODO Auto-generated method stub
		
		userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
		return userService.createUser(userDto);
		
	}

}

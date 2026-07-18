package com.substring.auth.services;

import com.substring.auth.dtos.UserDto;

public interface UserService {

	//Create User 
	UserDto createUser(UserDto userDto);
	
	
	//Get User by Email Id
	UserDto getUserByEmail(String email);
	
	UserDto findById(String userId);
	
	
	//Update the user
	UserDto updateUser(UserDto userDto, String userId);
	
	//Delete User
	void deleteUser(String userId);
	
	Iterable<UserDto> getAllUsers();
}

package com.substring.auth.services;



import java.time.Instant;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.substring.auth.dtos.UserDto;
import com.substring.auth.entities.Provider;
import com.substring.auth.entities.User;
import com.substring.auth.exceptions.ResourceNotFoundException;
import com.substring.auth.helpers.UserHelper;
import com.substring.auth.repositories.UserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepository;
	
	private final ModelMapper modelMapper;

	@Override
	public UserDto createUser(UserDto userDto) {
		if(userDto.getEmail() == null || userDto.getEmail().isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		
		if(userRepository.existsByEmail(userDto.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}
		
		User user = modelMapper.map(userDto, User.class); 
		
		user.setEnable(true);
		
		user.setProvider(userDto.getProvider()!=null?userDto.getProvider():Provider.LOCAL);
		
		User savedUser = userRepository.save(user);
		return modelMapper.map(savedUser, UserDto.class);
	}

	@Override
	public UserDto getUserByEmail(String email) {
		// TODO Auto-generated method stub
		
		User user = userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User not found"));
		return modelMapper.map(user,UserDto.class);
		
	}

	@Override
	public UserDto updateUser(UserDto userDto, String userId) {
		// TODO Auto-generated method stub
		UUID uId = UserHelper.parseUUID(userId);
		
		User existingUser = userRepository.findById(uId).orElseThrow(()-> new ResourceNotFoundException("User not found with the given ID"));
		
		if(userDto.getName()!=null) existingUser.setName(userDto.getName());
		
		if(userDto.getImage()!=null) existingUser.setImage(userDto.getImage());
		
		if(userDto.getProvider()!=null) existingUser.setProvider(userDto.getProvider());
		
		if(userDto.getPassword()!=null) existingUser.setPassword(userDto.getPassword());
		
		existingUser.setEnable(userDto.getEnable());
		existingUser.setUpdatedAT(Instant.now());
		
		User updateUser = userRepository.save(existingUser);
		return modelMapper.map(updateUser, UserDto.class);
	}

	@Override
	public void deleteUser(String userId) {
		// TODO Auto-generated method stub
		UUID uId = UserHelper.parseUUID(userId);
		User user = userRepository.findById(uId).orElseThrow(()-> new ResourceNotFoundException("User not found with the given Id"));
		userRepository.delete(user);
	}

	@Override
	public Iterable<UserDto> getAllUsers() {
		// TODO Auto-generated method stub
		return userRepository.findAll().stream()
		        .map((User user) -> modelMapper.map(user, UserDto.class))
		        .toList();
	}

	@Override
	public UserDto findById(String userId) {
		// TODO Auto-generated method stub
		
		UUID uId = UserHelper.parseUUID(userId);
		User user = userRepository.findById(uId).orElseThrow(()-> new ResourceNotFoundException("User not found with the given Id"));
		return modelMapper.map(user,UserDto.class);
	}

}

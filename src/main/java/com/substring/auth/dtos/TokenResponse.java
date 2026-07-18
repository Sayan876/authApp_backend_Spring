package com.substring.auth.dtos;

import com.substring.auth.entities.User;

public record TokenResponse(
		
		String accessToken,
		String refreshToken,
		long expiresIn,
		String tokenType,
		UserDto user
		
		
		) {
	
	public static TokenResponse of(String accessToken, String refreshToken, long expiresIn, String tokenType, UserDto user) {
		return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
	}
	

}

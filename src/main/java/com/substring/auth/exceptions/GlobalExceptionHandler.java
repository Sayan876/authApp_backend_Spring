package com.substring.auth.exceptions;

import javax.naming.AuthenticationException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.CredentialExpiredException;

import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.substring.auth.dtos.ApiError;
import com.substring.auth.dtos.ErrorResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	
	@ExceptionHandler({
		UsernameNotFoundException.class,
		BadCredentialsException.class,
		CredentialExpiredException.class,
		DisabledException.class
		
	})
	public ResponseEntity<ApiError> handleAuthException(Exception e , HttpServletRequest request){
		
		var apiError = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", e.getMessage(), request.getRequestURI());
		return ResponseEntity.badRequest().body(apiError);
	}
	
	@ExceptionHandler(ResourceNotFoundException.class)
   public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException exception){
	   ErrorResponse internelServerError = new ErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND,404);
	   return ResponseEntity.status(HttpStatus.NOT_FOUND).body(internelServerError);
   }
	
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception){
		 ErrorResponse internelServerError = new ErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST,400);
		   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(internelServerError);
	}
}

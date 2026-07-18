package com.substring.auth.controllers;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.substring.auth.dtos.LoginRequest;
import com.substring.auth.dtos.RefreshTokenRequest;
import com.substring.auth.dtos.TokenResponse;
import com.substring.auth.dtos.UserDto;
import com.substring.auth.entities.RefreshToken;
import com.substring.auth.entities.User;
import com.substring.auth.repositories.RefreshTokenRepository;
import com.substring.auth.repositories.UserRepository;
import com.substring.auth.security.CookieService;
import com.substring.auth.security.JwtService;
import com.substring.auth.services.AuthService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("api/v1/auth")
@AllArgsConstructor

public class AuthController {

	private final AuthService authService;

	private final RefreshTokenRepository refreshTokenRepository;
	private final AuthenticationManager authenticationManager;

	private final UserRepository userRepository;

	private final JwtService jwtService;

	private final ModelMapper modelMapper;

	private final CookieService cookieService;
	
	

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {

		Authentication authenticate = authenticate(loginRequest);
		User user = userRepository.findByEmail(loginRequest.email())
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

		if (!user.isEnable()) {
			throw new DisabledException("User is disabled");
		}

		String jti = UUID.randomUUID().toString();

		var refreshTokenDb = RefreshToken.builder().jti(jti).user(user).createdAt(Instant.now())
				.expiresdAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds())).revoked(false).build();

		refreshTokenRepository.save(refreshTokenDb);

		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.generateRefreshToken(user, refreshTokenDb.getJti());

		cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());
		cookieService.addNoStoreHeaders(response);

		TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTtlSeconds(),
				"Bearer", modelMapper.map(user, UserDto.class));
		return ResponseEntity.ok(tokenResponse);
	}

	public Authentication authenticate(LoginRequest loginRequest) {
		try {
			return authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new BadCredentialsException("Wrong Username or Password");
		}
	}
	
	@PostMapping("/refresh")

	public ResponseEntity<TokenResponse> refreshToken(@RequestBody(required = false) RefreshTokenRequest body,
			HttpServletResponse response, HttpServletRequest request) throws InterruptedException {
		
		Thread.sleep(5000);

		String refreshToken = readRefreshTokenFromRequest(body, request).orElseThrow(()->new BadCredentialsException("Refresh Token is missing"));

		if(!jwtService.isRefreshToken(refreshToken)) {
			throw new BadCredentialsException("Invalid Refresh Token");
		}
		
		String jti = jwtService.getJti(refreshToken);
		UUID userId = jwtService.getUserId(refreshToken);
		
		RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti).orElseThrow(()-> new BadCredentialsException("Invalid Refresh Token"));
		
		if(storedRefreshToken.isRevoked()) {
			throw new BadCredentialsException("The refresh token is already revoked");
		}
		
		if(storedRefreshToken.getExpiresdAt().isBefore(Instant.now())) {
			throw new BadCredentialsException("The refresh token has expired");
		}
		
		if(!storedRefreshToken.getUser().getId().equals(userId)) {
			throw new BadCredentialsException("This refresh token doesn't belong the the user");
		}
		
		
		storedRefreshToken.setRevoked(true);
		String newJti = UUID.randomUUID().toString();
		storedRefreshToken.setReplacedByTaken(newJti);
		refreshTokenRepository.save(storedRefreshToken);
		
		
		User user = storedRefreshToken.getUser();
		
		var newRefreshTokenDb = RefreshToken.builder()
		        .jti(newJti)
		        .user(user)
		        .createdAt(Instant.now())
		        .expiresdAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
		        .revoked(false)
		        .build();
		
		refreshTokenRepository.save(newRefreshTokenDb);
		String newAccesstoken = jwtService.generateAccessToken(user);
		String newRefreshToken1 = jwtService.generateRefreshToken(user, newRefreshTokenDb.getJti());
		cookieService.attachRefreshCookie(response, newRefreshToken1,(int) jwtService.getRefreshTtlSeconds());
		cookieService.addNoStoreHeaders(response);
		return ResponseEntity.ok(TokenResponse.of(newAccesstoken, newRefreshToken1, jwtService.getAccessTtlSeconds(), newRefreshToken1, modelMapper.map(user, UserDto.class)));
	}
	
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response){
		readRefreshTokenFromRequest(null, request).ifPresent((String token) -> {
		    try {
		        if (jwtService.isRefreshToken(token)) {
		            String jti = jwtService.getJti(token);

		            refreshTokenRepository.findByJti(jti).ifPresent((RefreshToken rt) -> {
		                rt.setRevoked(true);
		                refreshTokenRepository.save(rt);
		            });
		        }
		    } catch (JwtException ignored) {
		    }
		});
		
		cookieService.clearRefreshCookie(response);
		cookieService.addNoStoreHeaders(response);
		SecurityContextHolder.clearContext();
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	
	private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request){
		
		if(request.getCookies()!=null) {
			Optional<String> fromCookie =  Arrays.stream(request.getCookies())
			.filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))

			.map(c -> c.getValue())
			.filter(v -> !v.isBlank())
			.findFirst();
			
			if(fromCookie.isPresent()) {
				return fromCookie;
			}
		}
		
		if(body!=null && body.refreshToken()!=null && !body.refreshToken().isBlank()) {
			return Optional.of(body.refreshToken());
		}
		
		// 3. custom header
		String refreshHeader = request.getHeader("X-Refresh-Token");

		if (refreshHeader != null && !refreshHeader.isBlank()) {
		    return Optional.of(refreshHeader.trim());
		}
		
		// Authorization = Bearer <token>
		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
		    String candidate = authHeader.substring(7).trim();

		    if (!candidate.isEmpty()) {
		        try {
		            if (jwtService.isRefreshToken(candidate)) {
		                return Optional.of(candidate);
		            }
		        } catch (Exception ignored) {
		        }
		    }
		}
		
		return Optional.empty();
		
		
	}

	@PostMapping("/register")
	public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
	}

}

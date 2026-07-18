package com.substring.auth.security;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.substring.auth.entities.Provider;
import com.substring.auth.entities.RefreshToken;
import com.substring.auth.entities.User;
import com.substring.auth.repositories.RefreshTokenRepository;
import com.substring.auth.repositories.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler{

	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final UserRepository userRepository;
	
	private final JwtService jwtService;
	
	private final CookieService cookieService; 
	
	private final RefreshTokenRepository refreshTokenRepository; 
	
	@Value("${app.auth.frontend.success-redirect}")
	private String frontEndSuccessUrl;
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		// TODO Auto-generated method stub
		logger.info("Successful Authentication");
		logger.info(authentication.toString());
		
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
		
		String registrationId = "Unknown";
		
		

		if (authentication instanceof OAuth2AuthenticationToken token) {
		    registrationId = token.getAuthorizedClientRegistrationId();
		}
		
		

		logger.info("registrationId: " + registrationId);
		logger.info("user: " + oAuth2User.getAttributes().toString());
		
		
		User user;

		switch (registrationId) {

		    case "google" ->{
		    	
		        String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();
		        String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
		        String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
		        String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();

		        User newUser = User.builder()
		                .email(email)
		                .name(name)
		                .image(picture)
		                .enable(true)
		                .provider(Provider.GOOGLE)
		                .providerId(googleId)
		                .build();

		       user = userRepository.findByEmail(email).orElseGet(()-> userRepository.save(newUser));
		    }
		    
		    case "github" -> {

		        String name = oAuth2User.getAttributes().getOrDefault("login", "").toString();
//		        Object emailObj = oAuth2User.getAttributes().get("email");
//		        String email = emailObj == null ? "" : emailObj.toString();
		        String email =(String) oAuth2User.getAttributes().getOrDefault("email", "");
		        if(email == null) {
		        	email = name + "@github.com";
		        }
		        String githubId = oAuth2User.getAttributes().getOrDefault("id", "").toString();
		        String image = oAuth2User.getAttributes().getOrDefault("avatar_url", "").toString();

		        User newUser = User.builder()
		                .email(email)
		                .name(name)
		                .image(image)
		                .enable(true)
		                .provider(Provider.GITHUB)
		                .providerId(githubId)
		                .build();
		        
		        user = userRepository.findByEmail(email).orElseGet(()-> userRepository.save(newUser));
		    }
		    
		    default -> {
		    	throw new RuntimeException("Invalid Registration ID");
		    }
		}
		
		
		String jti = UUID.randomUUID().toString();
		
		RefreshToken refreshTokenDB =  RefreshToken.builder()
		.jti(jti)
		.user(user)
		.revoked(false)
		.createdAt(Instant.now())
		.expiresdAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
		.build();
		
		refreshTokenRepository.save(refreshTokenDB);
		
		String accesstoken = jwtService.generateAccessToken(user);
		String refreshtoken = jwtService.generateRefreshToken(user, refreshTokenDB.getJti());
		cookieService.attachRefreshCookie(response, refreshtoken,(int) jwtService.getRefreshTtlSeconds());
		
//		response.getWriter().write("Login Success and Jesus loves you");
		
		response.sendRedirect(frontEndSuccessUrl);
		
	}

}

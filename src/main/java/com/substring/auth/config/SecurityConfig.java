package com.substring.auth.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.*;

import com.substring.auth.dtos.ApiError;
import com.substring.auth.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity

public class SecurityConfig {
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	@Autowired
	private AuthenticationSuccessHandler successHandler;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http.csrf(e -> e.disable())
		.cors(Customizer.withDefaults())
		.sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		.authorizeHttpRequests(authorizeHttpRequests -> 
		authorizeHttpRequests
		.requestMatchers(AppConstants.AUTH_PUBLIC_URLS).permitAll()
		.anyRequest().authenticated()
		
				)
		
		.oauth2Login(oauth2 -> oauth2.successHandler(successHandler).failureHandler(null)).logout(AbstractHttpConfigurer::disable)
		.exceptionHandling(ex-> ex.authenticationEntryPoint((request, response, e )->{
					
					response.setStatus(401);
					response.setContentType("application/json");
					
					String message = "unauthorized access " + e.getMessage();
					System.out.println(message);
					String error =(String) request.getAttribute("error");
					
					if(error!=null) {
						error = message;
					}
					
//					Map<String, String> errorMap = Map.of("message",message, "status",String.valueOf(401));
					var apiError = ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized !!!!!" , message, request.getRequestURI(),true);
					var objectMapper = new ObjectMapper();
					response.getWriter().write(objectMapper.writeValueAsString(apiError));
				}))
		
		.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		
		
		return http.build();
		
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
		return configuration.getAuthenticationManager();
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.front-end-url}") String corsUrl) {
		
		String[] urls = corsUrl.trim().split(",");
		
		
	    var config = new CorsConfiguration();
	    config.setAllowedOrigins(Arrays.asList(urls));
	    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
	    config.setAllowedHeaders(List.of("*"));
	    config.setAllowCredentials(true);
	    
	    var source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", config);
        return source;
	}

}

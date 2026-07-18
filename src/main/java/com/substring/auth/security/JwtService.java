package com.substring.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.substring.auth.entities.Role;
import com.substring.auth.entities.User;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;

import javax.crypto.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;

import io.jsonwebtoken.Jwts;

@Service
@Getter
@Setter
public class JwtService {

	
	private final SecretKey key;
	private final long accessTtlSeconds;
	private final long refreshTtlSeconds;
	private final String issuer; 
	
	public JwtService(@Value("${security.jwt.secret}") String secret,
			          @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds, 
			          @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds, 
			          @Value("${security.jwt.issuer}") String issuer) {
		
		
		if(secret==null || secret.length()<64) {
			throw new IllegalArgumentException("Invalid key");
		}
		
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessTtlSeconds = accessTtlSeconds;
		this.refreshTtlSeconds = refreshTtlSeconds;
		this.issuer = issuer;
	}
	
	
	//To generate access token
	public String generateAccessToken(User user) {

	    Instant now = Instant.now();

	    List<String> roles = user.getRoles() == null
	            ? List.of()
	            : user.getRoles()
	                  .stream()
	                  .map(Role::getName)
	                  .toList();

	    return Jwts.builder()
	            .id(UUID.randomUUID().toString())
	            .subject(user.getId().toString())
	            .issuer(issuer)
	            .issuedAt(Date.from(now))
	            .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
	            .claims(Map.of(
	                    "email", user.getEmail(),
	                    "roles", roles,
	                    "typ", "access"
	            ))
	            .signWith(key, Jwts.SIG.HS512)
	            .compact();
	}
	
	// To generate refresh token
	public String generateRefreshToken(User user, String jti) {

	    Instant now = Instant.now();

	    return Jwts.builder()
	            .id(jti)
	            .subject(user.getId().toString())
	            .issuer(issuer)
	            .issuedAt(Date.from(now))
	            .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
	            .claims(Map.of(
	                    "typ", "refresh"
	            ))
	            .signWith(key, Jwts.SIG.HS512)
	            .compact();
	}
	
	
	// Parse and validate JWT
	public Jws<Claims> parse(String token) {
	    try {
	        return Jwts.parser()
	                .verifyWith(key)
	                .build()
	                .parseSignedClaims(token);
	    } catch (JwtException e) {
	        throw e;
	    }
	}

	// Check if it is an access token
	public boolean isAccessToken(String token) {
	    Claims claims = parse(token).getPayload();
	    return "access".equals(claims.get("typ"));
	}

	// Check if it is a refresh token
	public boolean isRefreshToken(String token) {
	    Claims claims = parse(token).getPayload();
	    return "refresh".equals(claims.get("typ"));
	}
	
	
	// Get user ID from token
	public UUID getUserId(String token) {
	    Claims claims = parse(token).getPayload();
	    return UUID.fromString(claims.getSubject());
	}

	// Get JWT ID (jti) from token
	public String getJti(String token) {
	    return parse(token).getPayload().getId();
	}
	
	
	public List<String> getRoles(String token){
		Claims c = parse(token).getPayload();
		return (List<String>) c.get("Roles");
	}
	
	public String getEmail(String token) {
		Claims c = parse(token).getPayload();
		return (String) c.get("email");
	}
}

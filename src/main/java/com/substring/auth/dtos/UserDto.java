package com.substring.auth.dtos;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.substring.auth.entities.Provider;
import com.substring.auth.entities.Role;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {


	private UUID id; 
	
	private String name;
	
	
	private String email;
	
	private String password; 
	
	private String image; 
	
	private Boolean enable = true; 
	
	private Instant createAt = Instant.now();
	
	private Instant updatedAT = Instant.now();
	
	
	
	private Provider provider = Provider.LOCAL; 
	
	

	private Set<Role> roles = new HashSet<>();
	
	

}

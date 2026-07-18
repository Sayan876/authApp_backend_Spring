package com.substring.auth.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
		info = @Info(
				title = "Auth application build by Delta Tech.",
				description = "A Generic authentication app. it can be used in any kind of application.",
				contact = @Contact(
						name = "Sayan Datta",
						url = "abc.com",
						email = "abc@email.com"
						),
				version = "1.0",
				summary = "This app is very useful if you don't want to create an auth app from scracth"
				),
		security = {
				@SecurityRequirement(
					name = "bearerAuth"
				)
		}
		)

@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		scheme = "bearer",
		bearerFormat = "JWT"
		)
public class APIDocConfig {

}

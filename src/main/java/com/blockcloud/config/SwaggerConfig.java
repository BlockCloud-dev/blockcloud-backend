package com.blockcloud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	@Bean
	public OpenAPI openAPI() {
		SecurityScheme bearerAuth = new SecurityScheme()
			.type(Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(In.HEADER)
			.name("Authorization");

		SecurityRequirement securityRequirement = new SecurityRequirement()
			.addList("BearerAuth");

		return new OpenAPI()
			.info(new Info()
				.title("BlockCloud API Docs")
				.version("v1.0")
				.description("BlockCloud API Docs"))
			.addSecurityItem(securityRequirement)
			.components(new io.swagger.v3.oas.models.Components()
				.addSecuritySchemes("BearerAuth", bearerAuth));
	}
}
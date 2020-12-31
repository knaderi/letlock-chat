package com.landedexperts.letlock.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class LetLockChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(LetLockChatApplication.class, args);
	}
	
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*", "http://localhost:3000","http://localhost:3001");

            }
        };
    }

	@Bean
	public ObjectMapper jacksonBuilder() {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.registerModule(new VavrModule());
	}

}

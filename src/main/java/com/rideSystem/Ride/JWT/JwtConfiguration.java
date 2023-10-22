package com.rideSystem.Ride.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;


@Configuration
public class JwtConfiguration {
    @Bean
    public Claims jwtClaims(){
        return new DefaultClaims();
    }
}

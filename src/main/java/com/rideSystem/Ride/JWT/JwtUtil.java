package com.rideSystem.Ride.JWT;



import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
@Slf4j
@Component
@AllArgsConstructor
public class JwtUtil {
    //using this to try and hide the secret key
    //Dotenv dotenv = Dotenv.load();
    //private final String secretKey = dotenv.get("SECRET")
    //;

    public JwtUtil(){

    }
    private final String secret = "salaisuus";

    private Set<String> invalidatedTokens = new HashSet<>();
    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public Date extractExpirationDate(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private boolean hasTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    public String generateToken(String username, String role){
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username);
    }
    private String createToken(Map<String, Object> claims, String username){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 2 ))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    public boolean validateToken(String token, UserDetails userDetails){
        log.info("token {}",token);
        final String userName = extractUsername(token);
        return (userName.equals(userDetails.getUsername()) && !hasTokenExpired(token));
    }

    public void expireToken(String token){
        invalidatedTokens.add(token);
    };

    public boolean isTokenExpired(String token){
        return invalidatedTokens.contains(token);
    }
}
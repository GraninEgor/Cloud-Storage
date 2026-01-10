package org.example.cloudstorage.core.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.cloudstorage.api.dto.response.AccessAndRefreshTokenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtService {

    @Value("mysupersecretkeymysupersecretkeymysupersecretkey")
    private String jwtSecret;

    public AccessAndRefreshTokenDto generateAuthTokens(String username){
        return AccessAndRefreshTokenDto.builder()
                .accessToken(generateAccessToken(username))
                .refreshToker(generateRefreshToken(username))
                .build();
    }

    public AccessAndRefreshTokenDto refresh(String username, String refreshToken){
        return AccessAndRefreshTokenDto.builder()
                .accessToken(generateAccessToken(username))
                .refreshToker(refreshToken)
                .build();
    }


    private String generateAccessToken(String username){
        Date date = Date.from(LocalDateTime.now().plusMinutes(30).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(date)
                .signWith(getSignInKey())
                .compact();
    }

    private String generateRefreshToken(String username){
        Date date = Date.from(LocalDateTime.now().plusDays(3).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(date)
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateJwtToken(String token){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}

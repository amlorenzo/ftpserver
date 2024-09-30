package com.irg.ftpserver.config.security;

import com.irg.ftpserver.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Service
@Data
@Conditional(JwtServiceKeyCondition.class)
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    public String generateToken(User user) {

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + expiration); // Set expiration

        logger.info("Generated JWT for user: {}, with role: {}", user.getUsername(), user.getRole());

        return Jwts.builder()
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .claim("email", user.getEmail())
                .issuer(issuer)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(decodedKey,"HmacSHA512");
    }

    public Claims decodeToken(String token) {
        Claims claim = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        logger.info("Decoded JWT for token: {}", token);
        return claim;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = decodeToken(token); // Decode the token
            boolean isExpired = isTokenExpired(claims);
            logger.info("Token validation result for {}: {}", token, !isExpired);
            return !isExpired; // Check if token is expired
        } catch (ExpiredJwtException e) {
            logger.warn("Token has expired: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            logger.warn("Invalid token signature: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            logger.warn("Invalid token: {}", e.getMessage());
            return false;
        }
    }
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration(); // Get expiration date
        return expiration.before(new Date()); // Check if the token is expired
    }


}

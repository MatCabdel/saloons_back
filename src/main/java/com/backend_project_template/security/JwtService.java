package com.backend_project_template.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private static final long TOKEN_VALIDITY_HOURS = 2L;

  @Value("${security.jwt.secret-key}")
  private String secretKey;

  @Value("${security.jwt.expiration-time}")
  private long jwtExpiration;

    private Key signingKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes());
  }


  public String generateToken(UserDetails userDetails) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + TimeUnit.HOURS.toMillis(TOKEN_VALIDITY_HOURS));
    return Jwts.builder()
      .setSubject(userDetails.getUsername())
      .claim("roles", userDetails.getAuthorities())
      .setIssuedAt(now)
      .setExpiration(expiry)
      .signWith(signingKey(), SignatureAlgorithm.HS256)
      .compact();
  }

  public Claims extractClaims(String token) {
    return Jwts.parserBuilder()
      .setSigningKey(signingKey())
      .build()
      .parseClaimsJws(token)
      .getBody();
  }

  public boolean validateJwtToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(secretKey.getBytes()).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}

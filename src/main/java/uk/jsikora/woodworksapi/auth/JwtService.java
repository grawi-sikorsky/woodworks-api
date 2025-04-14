package uk.jsikora.woodworksapi.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.jsikora.woodworksapi.user.BaseUser;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${woodworks.jwt.secret}")
    private String jwtSecret;

    @Value("${woodworks.jwt.expiration}")
    private long jwtExpirationMillis;

    public String generateToken(BaseUser baseUser) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMillis);

        return Jwts.builder()
                   .setSubject(baseUser.getEmail())
                   .claim("id", baseUser.getId())
                   .claim("name", baseUser.getName())
                   .setIssuedAt(now)
                   .setExpiration(expiryDate)
                   .signWith(getSignKey(), SignatureAlgorithm.HS256)
                   .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getSignKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    // === Walidacja tokena względem użytkownika ===
    public boolean isTokenValid(String token, BaseUser user) {
        final String email = getEmailFromToken(token);
        return (email.equals(user.getEmail()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getSignKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    // === Pobranie klucza z sekretu ===
    private SecretKey getSignKey() {
        byte[] decodedKey = Base64.getEncoder()
                                  .encode(jwtSecret.getBytes());
        return Keys.hmacShaKeyFor(decodedKey);
    }
}
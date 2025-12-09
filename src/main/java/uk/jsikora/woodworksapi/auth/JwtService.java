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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for JWT token generation, validation, and extraction of claims.
 * Handles all JWT-related operations for user authentication.
 */
@Service
public class JwtService {

    @Value("${woodworks.jwt.secret}")
    private String jwtSecret;

    @Value("${woodworks.jwt.expiration}")
    private long jwtExpirationMillis;

    /**
     * Generates a JWT token for the given user with additional claims.
     * 
     * @param baseUser the user for whom to generate the token
     * @param extraClaims additional claims to include in the token
     * @return the generated JWT token string
     */
    public String generateToken(BaseUser baseUser, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMillis);

        return Jwts.builder()
                   .setClaims(extraClaims)
                   .setSubject(baseUser.getEmail())
                   .claim("id", baseUser.getId())
                   .claim("name", baseUser.getName())
                   .setIssuedAt(now)
                   .setExpiration(expiryDate)
                   .signWith(getSignKey(), SignatureAlgorithm.HS256)
                   .compact();
    }

    /**
     * Extracts the email (subject) from a JWT token.
     * 
     * @param token the JWT token
     * @return the email address stored in the token
     */
    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getSignKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .getSubject();
    }

    /**
     * Extracts the authentication provider from a JWT token.
     * 
     * @param token the JWT token
     * @return the provider name stored in the token
     */
    public String getProviderFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(getSignKey())
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

        return claims.get("provider").toString();
    }

    /**
     * Validates a JWT token by parsing and verifying its signature.
     * 
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
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

    /**
     * Validates a JWT token against a specific user.
     * Checks if the token's email matches the user's email and if the token is not expired.
     * 
     * @param token the JWT token to validate
     * @param user the user to validate against
     * @return true if the token is valid for the user, false otherwise
     */
    public boolean isTokenValid(String token, BaseUser user) {
        final String email = getEmailFromToken(token);
        return (email.equals(user.getEmail()) && !isTokenExpired(token));
    }

    /**
     * Checks if a JWT token is expired.
     * 
     * @param token the JWT token to check
     * @return true if the token is expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a JWT token.
     * 
     * @param token the JWT token
     * @return the expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a JWT token using a claims resolver function.
     * 
     * @param token the JWT token
     * @param claimsResolver function to extract the desired claim
     * @param <T> the type of the claim value
     * @return the extracted claim value
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a JWT token.
     * 
     * @param token the JWT token
     * @return the claims object containing all token claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(getSignKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    /**
     * Generates the signing key from the configured JWT secret.
     * 
     * @return the SecretKey for signing and verifying tokens
     */
    private SecretKey getSignKey() {
        byte[] decodedKey = Base64.getEncoder()
                                  .encode(jwtSecret.getBytes());
        return Keys.hmacShaKeyFor(decodedKey);
    }
}
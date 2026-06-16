package gestion.scolaire.util;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import gestion.scolaire.model.Utilisateur;
import gestion.scolaire.repository.UtilisateurRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Autowired
    UtilisateurRepository userRepository;

    
    public String generateToken(Utilisateur user) {

    String subject = null;

    if (user.getEmail() != null && !user.getEmail().isBlank()) {
        subject = user.getEmail();
    } else if (user.getTelephone() != null && !user.getTelephone().isBlank()) {
        subject = user.getTelephone();
    }

    if (subject == null) {
        throw new IllegalStateException(
                "Impossible de générer le JWT : email et téléphone absents"
        );
    }

    Map<String, Object> claims = new HashMap<>();
    claims.put("id", user.getId());
    claims.put("nom", user.getNom());
    claims.put("prenom", user.getPrenom());
    claims.put("email", user.getEmail());
    claims.put("telephone", user.getTelephone());
    claims.put("role", user.getRole() != null ? user.getRole().name() : null);

    return createToken(claims, subject);
    }


 
    private String createToken(Map<String, Object> claims, String subject) {
        // System.out.println("Claims ajoutés : " + claim!s);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();

    }

    private Key getSignKey() {
        byte[] keyBytes = secretKey.getBytes();
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignKey())
                .parseClaimsJws(token)
                .getBody();
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extraire les informations personnalisées (nom, téléphone, etc.)
    public Map<String, Object> extractAdditionalInfo(String token) {
        final Claims claims = extractAllClaims(token);
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("id", claims.get("id"));
        additionalInfo.put("nom", claims.get("nom"));
        additionalInfo.put("prenom", claims.get("prenom"));
        additionalInfo.put("telephone", claims.get("telephone"));

        System.out.println("JWT Claims: " + claims);
        System.out.println("Extracted Info: " + additionalInfo);

        return additionalInfo;
    }
    
}


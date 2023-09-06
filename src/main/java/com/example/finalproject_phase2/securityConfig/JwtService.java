package com.example.finalproject_phase2.securityConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private static final String SECRET_KEY="1e5df348036444f200b97df001d8f02b3bf8ede372ed400c8022194b41020afe";
    public String extractUsername(String token){
        return extractClaim(token,Claims::getSubject);
    }
    public <T>T extractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims=extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    public String generateToken(UserDetails userDetails){
       return generateToken(new HashMap<>(),userDetails);
    }
    public boolean isTokenValid(String token,UserDetails userDetails){
        final String username=extractUsername(token);
        return username.equals(userDetails.getUsername())&& !isTokenExpired(token);
    }
    private boolean isTokenExpired(String token){
        return extractExpression(token).before(new Date());
    }
    private Date extractExpression(String token){
      return  extractClaim(token,Claims::getExpiration);
    }
    public String generateToken(Map<String,Object> extraClaims, UserDetails userDetails){
return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername()).
        setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis()*1000*60*24))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256).compact();
    }
    public Claims extractAllClaims(String token){
        return Jwts.parserBuilder().setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private Key getSignInKey(){
        byte[] keyBytes= Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

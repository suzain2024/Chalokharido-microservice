package com.example.api_gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.bouncycastle.jcajce.provider.digest.SHA1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;
public void validateToken(String token)
{
    Jwts.parser()
            .verifyWith(getSignKey())
            .build()
            .parseSignedClaims(token);
}
    // Reads user details from inside the token:
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private SecretKey getSignKey() {
    byte[] keys=Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keys);

    }


}
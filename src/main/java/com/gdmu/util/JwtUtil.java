package com.gdmu.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(Long userId, String openid) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return JWT.create()
                .withSubject(userId.toString())
                .withClaim("openid", openid)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public String generateToken(Long userId, String openid, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return JWT.create()
                .withSubject(userId.toString())
                .withClaim("openid", openid)
                .withClaim("role", role)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public String getRoleFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt != null) {
            return jwt.getClaim("role").asString();
        }
        return null;
    }

    public DecodedJWT verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt != null) {
            return Long.parseLong(jwt.getSubject());
        }
        return null;
    }

    public String getOpenidFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt != null) {
            return jwt.getClaim("openid").asString();
        }
        return null;
    }
}

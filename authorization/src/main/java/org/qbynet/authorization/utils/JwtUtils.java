package org.qbynet.authorization.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.qbynet.authorization.entity.Account;
import org.qbynet.authorization.service.AccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;

    @Resource
    AccountService accountService;

    public boolean invalidateJwt(String headerToken) {
        String token = convertToken(headerToken);
        if (token == null) return false;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = verifier.verify(token);
            String id = jwt.getId();
            return deleteToken(id, jwt.getExpiresAt()); // delete token
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    private boolean deleteToken(String uuid, Date time) {
        // do delete
        if (isInvalidToken(uuid)) {
            return false;
        }
        Date now = new Date();
        long expire = Math.max(time.getTime() - now.getTime(), 0);
        if (expire == 0) {
            return false;
        }
        // add to blocklist
        template.opsForValue().set(Const.JWT_BLACKLIST + uuid, "", expire, TimeUnit.MILLISECONDS);
        return true;
    }

    public boolean isInvalidToken(String uuid) {
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACKLIST + uuid));
    }

    private boolean isExpire() {
        return false;
    }

    /**
     * 解析JWT
     * */
    public DecodedJWT resolveJwt(String headerToken) {
        String token = this.convertToken(headerToken);
        if (token == null) {
            return null; // incorrect token
        }
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try {
            DecodedJWT jwt = jwtVerifier.verify(token);
            if (isInvalidToken(jwt.getId())) {
                // blocked token
                return null;
            }
            Date expireAt = jwt.getExpiresAt();
            return new Date().after(expireAt) ? null : jwt;
        } catch (JWTVerificationException error) {
            // User modified this
            return null;
        }
    }

    public String getUsernameFromJwtToken(String token) {
        DecodedJWT decodedJWT = resolveJwt(token);
        UserDetails userDetails = toUser(decodedJWT);
        Account account = accountService.findAccountByUsernameOrEmail(userDetails.getUsername());
        if (account == null) {
            return null;
        } else {
            return account.getUsername();
        }
    }

    public String createJwt(@NotNull UserDetails details, int id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        return JWT.create()
                .withJWTId(String.valueOf(UUID.randomUUID()))
                .withClaim("id", id) // id
                .withClaim("name", username) // username
                .withClaim("authorities", details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(getExpireDate()) // now + {date}
                .withIssuedAt(new Date()) // time now
                .sign(algorithm);
    }

    public Date getExpireDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 24 * expire);
        return calendar.getTime();
    }

    private String convertToken(String headerToken) {
        if (headerToken == null || !headerToken.startsWith("Bearer ")) {
            return null; // incorrect token
        }
        // cut "Bearer "
        return headerToken.substring(7);
    }

    public UserDetails toUser(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
        return User.withUsername(claims.get("name").asString())
                .password("I-HAVN'T-SAVED-PASSWORD")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    public Integer getId(DecodedJWT jwt) {
        return jwt.getClaims().get("id").asInt();
    }
}

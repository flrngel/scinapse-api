package network.pluto.absolute.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import network.pluto.absolute.model.LoginUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TokenHelper {

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.cookie}")
    private String cookie;

    @Value("${jwt.expires-in}")
    private int expireIn;

    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token, Claims::getExpiration);
    }

    public Boolean canTokenBeRefreshed(String token) {
        return getExpirationDateFromToken(token).compareTo(generateCurrentDate()) > 0;
    }

    public String refreshToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        claims.setIssuedAt(generateCurrentDate());
        return generateToken(claims);
    }

    public String generateToken(LoginUserDetails details) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", details.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(details.getMember().getEmail())
                .setIssuedAt(generateCurrentDate())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, secret)
                .compact();
    }


    private <T> T getClaimsFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.info("JWT Token is expired", e);
            throw new TokenExpiredException("JWT Token expired", token, e.getLocalizedMessage());
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            log.error("Invalid JWT Token", e);
            throw new TokenInvalidException("Invalid JWT token: ", token, e.getLocalizedMessage());
        }
    }

    String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, secret)
                .compact();
    }

    private long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    private Date generateCurrentDate() {
        return new Date(getCurrentTimeMillis());
    }

    private Date generateExpirationDate() {

        return new Date(getCurrentTimeMillis() + expireIn * 1000);
    }

    public String getToken(HttpServletRequest request) {
        // get token from cookie
        Cookie authCookie = getCookieValueByName(request, cookie);
        if (authCookie != null) {
            return authCookie.getValue();
        }

        // get token from header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public Cookie getCookieValueByName(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (int i = 0; i < request.getCookies().length; i++) {
            if (request.getCookies()[i].getName().equals(name)) {
                return request.getCookies()[i];
            }
        }
        return null;
    }
}

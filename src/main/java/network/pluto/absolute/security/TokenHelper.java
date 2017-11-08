package network.pluto.absolute.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public String refreshToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        claims.setIssuedAt(generateCurrentDate());
        return generateToken(claims);
    }

    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, secret)
                .compact();
    }

    public String generateToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", member.getId());
        claims.put("name", member.getName());
        claims.put("roles", member.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(member.getEmail())
                .setIssuedAt(generateCurrentDate())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, secret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token, Claims::getSubject);
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
            log.info("JWT Token is expired");
            throw new TokenExpiredException("JWT Token expired", token, e.getLocalizedMessage());
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            log.error("Invalid JWT Token", e);
            throw new TokenInvalidException("Invalid JWT token: ", token, e.getLocalizedMessage());
        }
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

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }

        return null;
    }

    public void addCookie(HttpServletResponse response, String token) {
        Cookie authCookie = new Cookie(cookie, token);
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(expireIn);
        response.addCookie(authCookie);
    }

    public void deleteCookie(HttpServletResponse response) {
        Cookie authCookie = new Cookie(cookie, null);
        authCookie.setPath("/");
        authCookie.setMaxAge(0);
        response.addCookie(authCookie);
    }
}

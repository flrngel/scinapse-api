package network.pluto.absolute.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TokenHelper {

    @Value("${pluto.jwt.issuer}")
    private String issuer;

    @Value("${pluto.jwt.secret}")
    private String secret;

    @Value("${pluto.jwt.cookie}")
    private String cookie;

    @Value("${pluto.jwt.expires-in}")
    private int expiresIn;

    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    public String refreshToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(generateCurrentDate())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, secret)
                .compact();
    }

    public String generateToken(Member member, boolean oauthLogin) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", member.getId());
        claims.put("name", member.getName());
        claims.put("oauth", oauthLogin);
        claims.put("roles", member.getAuthorities().stream().map(Authority::getName).collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(member.getEmail())
                .setIssuedAt(generateCurrentDate())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, secret)
                .compact();
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
        return new Date(getCurrentTimeMillis() + expiresIn * 1000);
    }

    public String getToken(HttpServletRequest request) {

        // get token from cookie
        Cookie jwt = WebUtils.getCookie(request, cookie);
        if (jwt != null) {
            return jwt.getValue();
        }

        // get token from header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public void addCookie(HttpServletResponse response, String token) {
        Cookie authCookie = new Cookie(cookie, token);
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(expiresIn);
        response.addCookie(authCookie);
    }

    public void removeCookie(HttpServletResponse response) {
        Cookie authCookie = new Cookie(cookie, null);
        authCookie.setPath("/");
        authCookie.setMaxAge(0);
        response.addCookie(authCookie);
    }
}

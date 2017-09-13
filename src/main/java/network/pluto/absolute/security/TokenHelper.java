package network.pluto.absolute.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TokenHelper {

    @Value("${app.name}")
    private String appName;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.header}")
    private String authHeader;

    @Value("${jwt.cookie}")
    private String cookie;

    @Value("${jwt.expires-in}")
    private int expireIn;

    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token, Claims::getSubject);
    }

    public Boolean canTokenBeRefreshed(String token) {
        final Date expirationDate = getClaimsFromToken(token, Claims::getExpiration);
        return expirationDate.compareTo(generateCurrentDate()) > 0;
    }

    public String refreshToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        claims.setIssuedAt(generateCurrentDate());
        return generateToken(claims);
    }

    public String generateToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", member.getAuthorities().stream().map(Authority::getName).collect(Collectors.toList()));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(appName)
                .setSubject(member.getEmail())
                .setIssuedAt(generateCurrentDate())
                .setExpiration(generateExpirationDate())
                .signWith(SIGNATURE_ALGORITHM, secret)
                .compact();
    }


    private <T> T getClaimsFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
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
        String authHeader = request.getHeader(this.authHeader);
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

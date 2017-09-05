package network.pluto.absolute.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import network.pluto.absolute.user.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private TokenHelper tokenHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jwt.expires-in}")
    private long expireIn;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        clearAuthenticationAttributes(request);
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        String jws = tokenHelper.generateToken(user.getUsername());

        TokenState tokenState = new TokenState(jws, System.currentTimeMillis() + expireIn * 1000);
        String jwtResponse = objectMapper.writeValueAsString(tokenState);

        response.setContentType("application/json");
        response.getWriter().write(jwtResponse);
    }
}

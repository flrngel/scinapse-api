package network.pluto.absolute.security;

import network.pluto.absolute.user.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenHelper tokenHelper;

    @Value("${jwt.expires-in}")
    private long expireIn;

    @RequestMapping(value = "/auth/token", method = RequestMethod.POST)
    public TokenState generate(@RequestBody AuthRequest authRequest) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);

        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        String jws = tokenHelper.generateToken(user.getUsername());
        return new TokenState(jws, System.currentTimeMillis() + expireIn * 1000);
    }
}

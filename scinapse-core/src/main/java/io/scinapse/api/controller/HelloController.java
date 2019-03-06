package io.scinapse.api.controller;

import io.scinapse.api.security.jwt.JwtUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class HelloController {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public Object hello() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "hello, world.");
        return result;
    }

    @RequestMapping(value = "/world", method = RequestMethod.GET)
    public Object world() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "hello, world.");
        return result;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public Object user(@ApiIgnore JwtUser user) {
        return getMessage(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public Object admin(@ApiIgnore JwtUser user) {
        return getMessage(user);
    }

    private Object getMessage(JwtUser user) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "hello, " + user.getName() + ".");
        result.put("id", user.getId());
        result.put("email", user.getEmail());
        result.put("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        result.put("name", user.getName());
        return result;
    }

}

package network.pluto.absolute.example;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    @RequestMapping("/hello")
    public Hello hello() {
        Hello hello = new Hello();
        hello.setContent("hello, world.");
        return hello;
    }

    @RequestMapping("/admin")
    public Hello admin() {
        Hello hello = new Hello();
        hello.setContent("hello, admin.");
        return hello;
    }
}

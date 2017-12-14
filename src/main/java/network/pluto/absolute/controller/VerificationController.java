package network.pluto.absolute.controller;

import network.pluto.absolute.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationController {

    private final VerificationService verificationService;

    @Autowired
    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @RequestMapping(value = "/verification", method = RequestMethod.GET)
    public Result verify(@RequestParam String token) {
        verificationService.verify(token);
        return Result.success();
    }
}

package sit.tuvarna.bg.userservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crypto")
public class TestController {

    private final EncryptionService encryptionService;

    @Autowired
    public TestController(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @GetMapping("/encrypt")
    public String encrypt(@RequestParam String text) {
        return encryptionService.encrypt(text);
    }

    @PostMapping("/decrypt")
    public String decrypt(@RequestBody String text) {
        return encryptionService.decrypt(text);
    }
}

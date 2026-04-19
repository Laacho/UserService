package sit.tuvarna.bg.userservice.web;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sit.tuvarna.bg.userservice.user.service.UserService;
import sit.tuvarna.bg.userservice.web.dto.request.LoginRequest;
import sit.tuvarna.bg.userservice.web.dto.request.RegisterRequest;
import sit.tuvarna.bg.userservice.web.dto.response.TokenPairResponse;

@RestController
@RequestMapping("/api/v1/users")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    public ResponseEntity<TokenPairResponse> register(@Valid @RequestBody RegisterRequest request) {
        TokenPairResponse register = userService.register(request);
        return ResponseEntity.ok(register);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenPairResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenPairResponse tokens = userService.login(request);
        return ResponseEntity.ok(tokens);
    }
}

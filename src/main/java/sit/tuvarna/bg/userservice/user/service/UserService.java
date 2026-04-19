package sit.tuvarna.bg.userservice.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sit.tuvarna.bg.userservice.feign.service.AuthService;
import sit.tuvarna.bg.userservice.user.model.Roles;
import sit.tuvarna.bg.userservice.user.model.Status;
import sit.tuvarna.bg.userservice.user.model.User;
import sit.tuvarna.bg.userservice.user.repository.UserRepository;
import sit.tuvarna.bg.userservice.utils.EgnValidationResult;
import sit.tuvarna.bg.userservice.utils.EgnValidator;
import sit.tuvarna.bg.userservice.web.dto.request.LoginRequest;
import sit.tuvarna.bg.userservice.web.dto.request.RegisterRequest;
import sit.tuvarna.bg.userservice.web.dto.response.TokenPairResponse;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    public TokenPairResponse register(RegisterRequest request){

        if(userRepository.existsByUsername(request.getUsername())){
            //todo handle errors correctly
            throw new IllegalArgumentException("Username already exists");
        }
        //todo maybe add a email validation
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        EgnValidationResult result = EgnValidator.validate(request.getEgn());
        if(!result.isValid()){
            throw new IllegalArgumentException("Invalid EGN");
        }
        String hashedEgn = passwordEncoder.encode(request.getEgn());
        LocalDate birthDate = result.getBirthDate();

        User user = User.builder()
                .firstName(request.getFirstName())
                .secondName(request.getSecondName())
                .thirdName(request.getThirdName())
                .username(request.getUsername())
                .password(hashedPassword)
                .egn(hashedEgn)
                .role(Roles.CLIENT)
                .email(request.getEmail())
                .status(Status.PENDING_VERIFICATION)
                .birthDate(birthDate)
                .phoneNumber(request.getPhoneNumber())
                .lastLogin(LocalDate.now())
                .lastPasswordChange(LocalDate.now())
                .build();

        userRepository.save(user);
        return authService.issueTokens(user);
    }

    public TokenPairResponse login(LoginRequest request){
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("Invalid credentials");
        }
        user.setLastLogin(LocalDate.now());
        userRepository.save(user);

        return authService.issueTokens(user);
    }

    public UUID resolveUserId(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username"));
        return user.getId();
    }
}